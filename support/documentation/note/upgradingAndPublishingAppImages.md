# Upgrading and publishing app images
- At the moment, published app images are used for running integration tests and for starting up infrastructure via docker-compose scripts. In the future, the usage of published app images might
  be expanded as necessary.
- Republishing of app images is recommended every time when dependencies are upgraded or functionalities are added in published apps.

## Workflow hints for dependencies upgrade:
- upgrade dependencies
- update the version of local images if necessary (for example, when switching from the snapshot to the released version or vice versa)
- create local images
    - build app images with spring boot cloud native buildpacks support

          gw bootBuildImage

- run integration and component tests
- test app with infrastructure started via docker-compose

      cd support/docker
      ./dockerComposeInfrastructureUp.sh
      ./dockerComposeRdbmsMigration.sh

    - run local apps and execute some requests

- publish a local image to Docker Hub
  - if running on arm64 architectures (i.e., Apple Silicon M1, M2, M3 processors), build amd64 architecture images before pushing to the Docker Hub:

        export KLOKWRK_USE_DEFAULT_BOOT_IMAGE_BUILDER=true
        gw bootBuildImage
        export KLOKWRK_USE_DEFAULT_BOOT_IMAGE_BUILDER=false

  - push images

        docker login
        docker push klokwrkprj/cargotracking-booking-app-rdbms-management:[version_tag]
        docker push klokwrkprj/cargotracking-booking-app-commandside:[version_tag]
        docker push klokwrkprj/cargotracking-booking-app-queryside-projection-rdbms:[version_tag]
        docker push klokwrkprj/cargotracking-booking-app-queryside-view:[version_tag]

- delete local image
- rerun integration tests
- merge changes on github
