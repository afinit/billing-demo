#!/bin/bash

## 3 commands for loading different types of usage with different dates
echo "create usage"
curl --request POST \
  --url http://localhost:8080/usage/create \
  --header 'Content-Type: application/json' \
  --data '{
	"date": { "dateString": "2023-05-10"},
	"units": { "units": "StorageBytes"},
	"amount": 41
}'
echo

curl --request POST \
  --url http://localhost:8080/usage/create \
  --header 'Content-Type: application/json' \
  --data '{
	"date": { "dateString": "2023-06-10"},
	"units": { "units": "cpu" },
	"amount": 41
}'
echo

curl --request POST \
  --url http://localhost:8080/usage/create \
  --header 'Content-Type: application/json' \
  --data '{
	"date": { "dateString": "2023-06-10"},
	"units": { "units": "storagebytes" },
	"amount": 41
}'
echo

## 5 different get commands with various filters
echo "get all usage"
curl --request POST \
  --url http://localhost:8080/usage/get \
  --header 'Content-Type: application/json' \
  --data '{

}'
echo

echo "get usage after 2023-05-11"
curl --request POST \
  --url http://localhost:8080/usage/get \
  --header 'Content-Type: application/json' \
  --data '{
	"startDate": {"dateString": "2023-05-11"}
}'
echo

echo "get usage before 2023-05-11"
curl --request POST \
  --url http://localhost:8080/usage/get \
  --header 'Content-Type: application/json' \
  --data '{
	"endDate": {"dateString": "2023-05-11"}
}'
echo

echo "get all usage for storagebytes"
curl --request POST \
  --url http://localhost:8080/usage/get \
  --header 'Content-Type: application/json' \
  --data '{
	"usageUnits": {"units": "storagebytes"}
}'
echo

echo "get all invoiced usage"
curl --request POST \
  --url http://localhost:8080/usage/get \
  --header 'Content-Type: application/json' \
  --data '{
	"isInvoiced": true
}'
echo


## 2 different invoice generations with different date filters
echo "generate invoice for any usage before 2023-05-11"
curl --request POST \
  --url http://localhost:8080/invoice/generate \
  --header 'Content-Type: application/json' \
  --data '{
	"endDate": {"dateString": "2023-05-11"}
}'
echo

echo "generate invoice for any usage after 2023-05-01"
curl --request POST \
  --url http://localhost:8080/invoice/generate \
  --header 'Content-Type: application/json' \
  --data '{
	"startDate": {"dateString": "2023-05-01"}
}'
echo

## 2 different calls to get each of the previously generated invoices
echo "get invoice 1"
curl --request GET --url http://localhost:8080/invoice/1
echo

echo "get invoice 2"
curl --request GET --url http://localhost:8080/invoice/2
echo