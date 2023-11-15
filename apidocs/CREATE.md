**Create Gather**
----
모임 생성

* **URL**

  `/gathers`

* **Method:**

  `POST`

* **Data Params**
   
  **Required:**
  ```json
  {
    "deadLine" : "2077-10-07",
    "name" : "gatherName",
    "userId" : "059d43a8-8231-11ee-af5c-d3a47625207d",
    "description" : "description is nullable",
    "startDate" : "2077-10-09",
    "endDate" : "2077-11-12",
    "startTime" : "12:00:00",
    "endTime" : "19:20:00",
    "duration" : "01:10:00",
    "deadLine" : "2077-10-07"
  }
  ```

* **Success Response:**

    * **Code:** 201 <br />
      **Content:** <br/>
      ```json
      {
        "gatherId" : "6208bb08-8232-11ee-bf8f-773ff4aab5eb",
        "name" : "gatherName",
        "userId" : "059d43a8-8231-11ee-af5c-d3a47625207d",
        "description" : "description is nullable",
        "startDate" : "2077-10-09",
        "endDate" : "2077-11-12",
        "startTime" : "12:00:00",
        "endTime" : "19:20:00",
        "duration" : "01:10:00",
        "deadLine" : "2077-10-07",
        "state" : "OPEN"
      }  
      ```