call setenv

echo "Starting GUI..."

REM ::1 is IPv6 localhost for running Client same machine as the server.
REM replace ::1 with an IPv4 or IPv6 address with the remote server IP.
%javaCmd% com.orderfoodnow.pos.frontend.Client ::1
 
pause
