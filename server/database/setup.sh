#!/bin/bash
echo "Running setup-db script..."
echo "Attempting login to mysql monitor as root..."
sudo mysql -u root < ./setup-db.sql
if [ $? -eq 0 ]; then
        echo "Successfully setup db!"
        exit
fi

