from locust import HttpUser, task, between

class SmartSGUser(HttpUser):
    # Use a small wait time to simulate real users; constant(0) can sometimes 
    # overwhelm the local machine running Locust before it overwhelms the server.
    wait_time = between(0.1, 0.5)

    @task(3)
    def get_listings(self):
        self.client.get("/api/listings")



# locust -f "/Users/erica/Documents/MSBA/Sem2_CS5224/CS5224 project/SmartSG/smartsg-backend/src/test/java/com/nus/cs5224/smartsg/load-test.py" \
#   --host "http://smartsg-alb-1456112585.ap-southeast-1.elb.amazonaws.com" \
#   --users 200 \
#   --spawn-rate 10 \
#   --headless \
#   --run-time 300s