import predictionio
engine_client = predictionio.EngineClient(url="http://localhost:8000")
print engine_client.send_query({"projectId": "P1", "testDataPath": "/quickstartapp/testData.txt" })