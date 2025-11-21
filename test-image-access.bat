@echo off
echo ================================
echo TEST TRUY CAP ANH THUMBNAIL
echo ================================
echo.
echo Dang kiem tra ket noi den server...
echo.

curl -I http://localhost:8080/static/courses/html-css.jpg

echo.
echo ================================
echo KET QUA:
echo - Neu thay "HTTP/1.1 200" -^> THANH CONG!
echo - Neu thay "HTTP/1.1 403" -^> LOI! Server chua reload
echo - Neu thay "Connection refused" -^> Server chua chay
echo ================================
echo.
pause

