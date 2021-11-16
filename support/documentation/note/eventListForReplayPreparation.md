# Updating the list of serialized events for "events replay" testing

## Axon Server CE resources
- REST API link: http://localhost:8024/v1
- REST API documentation home link: http://localhost:8024/swagger-ui/index.html
- REST API documentation events-rest-controller link: http://localhost:8024/swagger-ui/index.html#/events-rest-controller

## Procedures
### Downloading event stream with browser
- for all events starting with global sequence 0

      http://localhost:8024/v1/events?timeout=3600&trackingToken=0

- same as previous but with some defaults specified explicitly

      http://localhost:8024/v1/events?allowSnapshots=true&initialSequence=0&timeout=3600&trackingToken=0

- for specific aggregate

      http://localhost:8024/v1/events?aggregateIdentifier=17dfba8d-0cfd-46e3-a56a-8e9de41aa641&allowSnapshots=true&initialSequence=0&timeout=3600&trackingToken=0

### Uploading events

- with curl

      curl -X POST "http://localhost:8024/v1/events" -H  "accept: */*" -H  "AxonIQ-Context: default" -H  "Content-Type: application/json" -d "{\"messages\": [{\"messageIdentifier\":\"36f5c505-97c2-47fc-ae1c-7f126aa5a1dd\",\"aggregateIdentifier\":\"41ba19d7-b4b2-4cd6-a31c-6e5f2f1a1253\",\"aggregateSequenceNumber\":0,\"aggregateType\":\"CargoAggregate\",\"payload\":{\"type\":\"org.klokwrk.cargotracker.booking.commandside.cargobook.axon.api.CargoBookedEvent\",\"data\":\"{\\\"aggregateIdentifier\\\":\\\"41ba19d7-b4b2-4cd6-a31c-6e5f2f1a1253\\\",\\\"originLocation\\\":{\\\"unLoCode\\\":{\\\"code\\\":\\\"HRRJK\\\"},\\\"name\\\":{\\\"name\\\":\\\"Rijeka\\\"},\\\"countryName\\\":{\\\"name\\\":\\\"Croatia\\\"},\\\"unLoCodeFunction\\\":{\\\"functionEncoded\\\":\\\"1234----\\\"}},\\\"destinationLocation\\\":{\\\"unLoCode\\\":{\\\"code\\\":\\\"HRZAG\\\"},\\\"name\\\":{\\\"name\\\":\\\"Zagreb\\\"},\\\"countryName\\\":{\\\"name\\\":\\\"Croatia\\\"},\\\"unLoCodeFunction\\\":{\\\"functionEncoded\\\":\\\"-2345---\\\"}}}\",\"revision\":\"\"},\"timestamp\":1596560370523,\"metaData\":{\"wf-ot-spanid\":\"195d82ad-f95b-48bb-8211-241a2a42b777\",\"wf-ot-sample\":\"true\",\"wf-ot-traceid\":\"a2998794-2f62-402e-b725-d8daa23839d3\",\"INBOUND_CHANNEL_TYPE\":\"web\",\"INBOUND_CHANNEL_REQUEST_IDENTIFIER\":\"127.0.0.1\",\"INBOUND_CHANNEL_NAME\":\"booking\"}}]}"

  - payload structure in command above:

        {
          "messages": [
            {
              "aggregateIdentifier": "string",
              "aggregateSequenceNumber": 0,
              "aggregateType": "string",
              "messageIdentifier": "string",
              "metaData": {
                "additionalProp1": {},
                "additionalProp2": {},
                "additionalProp3": {}
              },
              "payload": {
                "data": "string",  // -> string escaped JSON
                "revision": "string",
                "type": "string"
              },
              "timestamp": 0
            }
          ]
        }
