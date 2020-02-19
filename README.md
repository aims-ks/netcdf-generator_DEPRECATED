TODO Add documentation about **ucar** documented standards (attributes starting with "_")
and **edal** undocumented mess (regex on some attribute values).

Add info about tools to look at the file
- Dive
- Panoply

Info about connecting to the MongoDB

1. Download the SSH key from the download database

2. Change SSH key access right to "400"

3. Get the server IP from AWS

4. Connect to the server using the SSH key

    ```$ ssh -i ereefs-tunnel.pem ec2-user@52.62.64.114```

5. Connect to the MongoDB docker image

    ```$ docker exec -it mongodb bash```

6. Find the username / password of the database from

    *AWS Systems Manager > Application Management > Parameter Store*

7. Connect to the database:
    ```
    $ mongo -u admin -p
    MongoDB shell version v4.2.3
    Enter password:
    ```

8. Connect to the eReefs database
    ```
    > use ereefs
    switched to db ereefs
    > help
    ```
