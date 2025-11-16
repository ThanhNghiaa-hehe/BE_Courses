@echo off
echo ========================================
echo   QUICK TEST - COURSE APIs
echo ========================================
echo.

echo [1/5] Testing Public API - Get All Published Courses...
curl -X GET http://localhost:8080/api/courses
echo.
echo.

echo [2/5] Testing if server is running...
curl -X GET http://localhost:8080/api/courses -s -o nul -w "HTTP Status: %%{http_code}\n"
echo.

echo ========================================
echo   TEST COMPLETE
echo ========================================
echo.
echo Next steps:
echo 1. Import "Course_API_Postman_Collection.json" vao Postman
echo 2. Doc file "COURSE_API_TEST_GUIDE.md" de biet chi tiet
echo 3. Xem cac mau JSON trong "COURSE_API_JSON_SAMPLES.md"
echo.
pause

