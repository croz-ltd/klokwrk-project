# Upgrading and publishing app images
- At the moment, published app images are used for running integration tests and for starting up infrastructure via docker-compose scripts. In the future, the usage of published app images might
  be expanded as necessary.
- Republishing of app images is recommended every time when dependencies are upgraded or functionalities are added in published apps.

## Workflow hints for dependencies upgrade:
- upgrade dependencies
- update version of local image if necessary (i.e. when switching from the snapshot to the released version or vice versa)
- create local image
    - build app images with spring boot cloud native buildpacks support

          gw bootBuildImage

    - tag images with versions used in integration tests. Replace [image-version] with i.e. 0.0.3-SNAPSHOT

          docker image tag cargotracker-booking-rdbms-management-app:[image-version] klokwrkprj/cargotracker-booking-rdbms-management-app:[image-version]
          docker image tag cargotracker-booking-commandside-app:[image-version] klokwrkprj/cargotracker-booking-commandside-app:[image-version]
          docker image tag cargotracker-booking-queryside-rdbms-projection-app:[image-version] klokwrkprj/cargotracker-booking-queryside-rdbms-projection-app:[image-version]
          docker image tag cargotracker-booking-queryside-app:[image-version] klokwrkprj/cargotracker-booking-queryside-app:[image-version]

- run integration tests
- test app with infrastructure started via docker-compose

      cd support/docker
      ./dockerComposeInfrastructureUp.sh
      ./dockerComposeRdbmsMigration.sh

    - run local apps and execute some requests

- publish a local image to Docker Hub

    docker login
    docker push klokwrkprj/cargotracker-booking-rdbms-management-app
    docker push klokwrkprj/cargotracker-booking-commandside-app
    docker push klokwrkprj/cargotracker-booking-queryside-rdbms-projection-app
    docker push klokwrkprj/cargotracker-booking-queryside-app

- delete local image
- rerun integration tests
- merge changes on github
