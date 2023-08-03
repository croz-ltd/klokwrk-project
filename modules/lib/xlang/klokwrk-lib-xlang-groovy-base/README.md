# klokwrk-lib-xlang-groovy-base

Module `klokwrk-lib-xlang-groovy-base` contains some general-purpose low-level utilities.

Those utilities classes include:
- general-purpose constants that can be used from any module in the project.
- methods for convenient fetching of object's properties and infrastructure helping with relaxing requirements of Groovy map constructor. These two features can help create immutable objects and
  support the simple mapping of data from one object into another. Quite often, this is more than enough for data mapping purposes without requiring any additional library.
- the lowest-level base DBC (Design-by-Contracts) methods
- other utilities
