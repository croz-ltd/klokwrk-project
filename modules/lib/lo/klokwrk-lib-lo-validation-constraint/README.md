# klokwrk-lib-lo-validation-constraint

Module `klokwrk-lib-lo-validation-constraint` contains annotations for custom Bean Validation constraints. The module depends only on `jakarta.validation:jakarta.validation-api` library, meaning that
concrete Bean Validation implementation is not pulled in from as a dependency of this module.

The module also includes `Level1` to `Level9` interfaces that are convenience intended to be used for specifying validation ordering through `jakarta.validation.GroupSequence` annotation.
