## sample_data.py

import mysql.connector
from mysql.connector import Error

# Connection parameters
host = "localhost"        # container mapped to host
port = 59557               # port we mapped in Docker
user = "root"
password = "password1"
database = "db1"

try:
    # Connect to MySQL
    connection = mysql.connector.connect(
        host=host,
        port=port,
        user=user,
        password=password,
        database=database
    )

    if connection.is_connected():
        print("Connected to MySQL!")

        cursor = connection.cursor()

        # Example 1: Create a table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50),
                email VARCHAR(100)
            )
        """)
        print("Table 'users' created.")

        # Example 2: Insert data
        cursor.execute("INSERT INTO users (name, email) VALUES (%s, %s)",
                       ("Alice", "alice@example.com"))
        cursor.execute("INSERT INTO users (name, email) VALUES (%s, %s)",
                       ("Bob", "bob@example.com"))

        connection.commit()
        print("Inserted 2 rows into 'users'.")

        # Example 3: Query data
        cursor.execute("SELECT * FROM users")
        rows = cursor.fetchall()
        
        for row in rows:
            print(row)

except Error as e:
    print("Error:", e)

