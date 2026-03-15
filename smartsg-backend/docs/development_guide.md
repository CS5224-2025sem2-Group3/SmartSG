# SmartSG Backend 开发指南

基于你的 API Spec 和架构图，以下是完整的开发步骤。

---

## 第一步：分析 API Spec → 提取 Entity

从 API 的请求/响应中反推出需要哪些数据库表：

| Entity | 对应的表 | 关键字段 | 来源 |
|--------|---------|---------|------|
| `User` | `users` | id, name, email, password | `/api/auth/*` 的请求/响应 |
| `Listing` | `listings` | id, title, type, totalRent, availableFrom, facilities | `/api/listings` 的响应 |
| `University` | `universities` | id, value, label | `/api/universities` 的响应 |
| `Favorite` | `favorites` | id, userId, listingId | `/api/favorites` 的响应 |
| `UserProfile` | `user_profiles` | userId, budgetMax, moveInWindow, leasePreference, sleepHabit, studyPreference, smoking, cleanliness | `/api/profile/me` 的响应 |
| `Group` | `groups` | id, listingId, status, requiredPeople, creatorId | `/api/groups` 的响应 |
| `GroupMember` | `group_members` | id, groupId, userId | 隐含的（用户加入/离开 group） |
| `Invitation` | `invitations` | id, groupId, fromUserId, toUserId, status | `/api/invitations` 的响应 |

### 表之间的关系

```
User 1 ──── 1 UserProfile          (一对一)
User 1 ──── N Favorite             (一对多)
User 1 ──── N Invitation           (一对多，作为发送者或接收者)
Listing 1 ── N Favorite            (一对多)
Listing 1 ── N Group               (一对多)
Group 1 ──── N GroupMember          (一对多)
Group 1 ──── N Invitation          (一对多)
User 1 ──── N GroupMember          (一对多)
```

---

## 第二步：创建包结构

在 `com.nus.cs5224.smartsg` 下创建以下包（文件夹）：

```
smartsg/
├── config/              # 配置类
├── controller/          # REST Controller（7个）
├── dto/                 # DTO
│   ├── request/         # 请求 DTO
│   └── response/        # 响应 DTO
├── entity/              # JPA Entity（8个）
├── repository/          # Spring Data Repository（8个）
├── service/             # Service 接口
│   └── impl/            # Service 实现
├── exception/           # 自定义异常
└── enums/               # 枚举（GroupStatus, InvitationStatus 等）
```

> [!TIP]
> 在 VS Code 或 IntelliJ 中直接新建 package，IDE 会自动创建对应的文件夹。

---

## 第三步：写 Entity（从下往上）

**顺序**：先写没有外键依赖的 → 再写有依赖的

### 3.1 先写 `User` Entity（无外键依赖）
```java
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    // Lombok: @Data @NoArgsConstructor @AllArgsConstructor @Builder
}
```

**你需要学的注解**：
- `@Entity` — 标记这是一个 JPA 实体
- `@Table(name = "xxx")` — 指定表名
- `@Id` + `@GeneratedValue` — 主键 + 自增策略
- `@Column` — 可选，指定列名和约束

### 3.2 然后按顺序写其他 Entity
1. `University` (无依赖)
2. `Listing` (无依赖，`facilities` 可以用 `@ElementCollection`)
3. `UserProfile` (依赖 User → 用 `@OneToOne`)
4. `Favorite` (依赖 User + Listing → 用 `@ManyToOne`)
5. `Group` (依赖 Listing + User)
6. `GroupMember` (依赖 Group + User)
7. `Invitation` (依赖 Group + User)

### 关键注解速查

| 关系 | 注解 | 例子 |
|------|------|------|
| 一对一 | `@OneToOne` | User ↔ UserProfile |
| 多对一 | `@ManyToOne` + `@JoinColumn` | Favorite → User |
| 一对多 | `@OneToMany(mappedBy = "xxx")` | User → List\<Favorite\> |

> [!IMPORTANT]
> 每写完一个 Entity，**重启 Spring Boot**，看控制台 `Hibernate: create table ...` 日志，确认表被正确创建。因为你设了 `ddl-auto: update`，Hibernate 会自动建表。

---

## 第四步：写 Repository

每个 Entity 对应一个 Repository，**非常简单**：

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);  // Spring Data 自动实现！
}
```

**你需要了解**：
- `JpaRepository<Entity, ID类型>` 自带 `findById`, `findAll`, `save`, `delete` 等方法
- 按命名规则写方法名，Spring Data 会自动生成 SQL，例如：
  - `findByEmail` → `SELECT * FROM users WHERE email = ?`
  - `findByListingId` → `SELECT * FROM favorites WHERE listing_id = ?`
  - `findByGroupIdAndStatus` → `WHERE group_id = ? AND status = ?`

---

## 第五步：写 DTO

**为什么要 DTO？** 不直接返回 Entity，是因为：
1. Entity 有 password 等敏感字段不该返回
2. 前端需要的结构和数据库表结构可能不一样

根据 API Spec 中的 Request / Response 各写一个 DTO：

| API | Request DTO | Response DTO |
|-----|-------------|--------------|
| POST `/auth/login` | `LoginRequest` (email, password) | `UserResponse` (id, name, email) |
| POST `/auth/register` | `RegisterRequest` (name, email, password) | `UserResponse` |
| GET `/listings` | 无（用 `@RequestParam`） | `ListingResponse` / `ListingDetailResponse` |
| PUT `/profile/me` | `ProfileUpdateRequest` | `SuccessResponse` |
| POST `/groups` | `CreateGroupRequest` (listingId) | `CreateGroupResponse` (groupId) |
| POST `/invitations` | `CreateInvitationRequest` (groupId, candidateUserId) | `SuccessResponse` |

> [!TIP]
> 可以写一个通用的 `SuccessResponse`，多个 API 共用：
> ```java
> public class SuccessResponse {
>     private boolean success = true;
> }
> ```

---

## 第六步：写 Service

**先写接口，再写实现**。每个业务模块一个 Service：

```
AuthService       → 登录、注册、获取当前用户
ListingService    → 查询房源列表、详情
FavoriteService   → 收藏的增删查
ProfileService    → 用户偏好的查和改
GroupService      → 小组的创建、删除、离开、确认
MatchingService   → 推荐室友（核心业务逻辑）
InvitationService → 邀请的发送、接受、拒绝
```

Service 层是**最核心**的，业务逻辑都写在这里。例如 `AuthService`：

```java
public interface AuthService {
    UserResponse login(LoginRequest request);
    UserResponse register(RegisterRequest request);
    UserResponse getCurrentUser(Long userId);
}
```

实现类里注入 Repository 做数据库操作：

```java
@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    // 构造器注入
    // 实现方法...
}
```

---

## 第七步：写 Controller

到这一步就是"组装"，非常机械化：

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

**7 个 Controller 的映射**：

| Controller | Base Path | 方法数 |
|-----------|-----------|--------|
| `AuthController` | `/api/auth` | 4 |
| `ListingController` | `/api/listings` + `/api/universities` | 3 |
| `FavoriteController` | `/api/favorites` | 3 |
| `ProfileController` | `/api/profile` | 2 |
| `GroupController` | `/api/groups` | 5 |
| `MatchingController` | `/api/groups/{listingId}/recommendations` | 1 |
| `InvitationController` | `/api/invitations` | 4 |

---

## 第八步：配置 Security

你的 `pom.xml` 有 `spring-boot-starter-security`，**不配置的话所有请求都会被拦截**。

先写一个 `SecurityConfig`，初期放行所有请求：

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
```

> [!WARNING]
> 这只是开发阶段的配置！后面要加上 JWT 认证，只放行 `/api/auth/login` 和 `/api/auth/register`。

---

## 第九步：异常处理

写一个全局异常处理器，统一错误格式：

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleException(RuntimeException e) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", e.getMessage()));
    }
}
```

---

## 建议的开发节奏

> [!IMPORTANT]
> **不要一次写完所有模块！** 按模块纵向切片：

```
第 1 轮：Auth 模块
  Entity(User) → Repo → DTO → Service → Controller → 用 Postman 测试

第 2 轮：Listing 模块
  Entity(University, Listing) → Repo → DTO → Service → Controller → 测试

第 3 轮：Profile + Favorite 模块

第 4 轮：Group 模块

第 5 轮：Invitation + Matching 模块
```

每完成一轮，用 **Postman** 或 **curl** 测一下 API，确保跑通再做下一个。

---

## 需要用到的枚举

```java
// entity/enums/GroupStatus.java
public enum GroupStatus {
    RECRUITING, CONFIRMED, CANCELLED
}

// entity/enums/InvitationStatus.java
public enum InvitationStatus {
    PENDING, ACCEPTED, REJECTED
}
```
