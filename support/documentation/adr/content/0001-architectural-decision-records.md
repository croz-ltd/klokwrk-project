# ADR-0001 - Architectural Decision Records
* **Status: accepted**
* Dates: proposed - 2020-10-27
* Authors: Damir Murat (`damir.murat.git at gmail.com`)
* Reviewers: None

## Context
We want a light way means of tracking and describing architecturally significant decisions made during this project. Selected methodology should include appropriate templates and possibly other
tools to help with documentation consistency.

### Architectural Context
* System (`klokwrk-project`)

## Decision
**We will use the architecture decision log expressed as a time-ordered collection of Architecture Decision Records (ADRs).** Architecture decision log is nothing more than a simple index of all
available ADRs.

**We will use [custom ADR template](../template/template.md).** It includes parts of following *"standard"* ADR templates:
* [Michael Nygard's template](https://github.com/joelparkerhenderson/architecture_decision_record/blob/master/adr_template_by_michael_nygard.md)
* [MADR template](https://adr.github.io/madr/)
* [Ken Power's template](https://youtu.be/LFiTwqblqsk?t=1295)

## Consequences
### Positive
* ADRs provide a way for communicating, collaborating, and documenting architecturally significant decisions.
* Writing ADRs requires conscious thinking and light elaboration of significant decisions, which leads to better decisions.
* The provided architecture decision log helps with maintaining and onboarding.

### Negative
* Writing ADRs takes time. As we progress and gain experience, time requirements should shrink.
* Using custom template might cause problems with available ADR tools. However, we don't consider tooling support as important requirement.

### Neutral
* Writing ADRs requires some time to get used to, and have some (minimal) learning curve.

## Considered Options
* Not documenting critical decisions.
* Using more elaborated process, for example, using RFC documents and process as proposed by Patrik Kua in CROZ internal workshop.<br/>
  * We do not want any overhead at this point, so more light way approach is preferable.

## References
* [`klokwrk-project` architecture decision log](../index.md)
* [`klokwrk-project` custom ADR template](../template/template.md)

### ADR Methodology
* [Michael Nygard's article introducing ADRs.](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
* [SATURN 2017 Talk: Architecture Decision Records in Action - Michael Keeling](https://www.youtube.com/watch?v=41NVge3_cYo)
* [Communicating and documenting architectural decisions - David Ayers](https://www.youtube.com/watch?v=rwfXkSjFhzc)
* [Architectural Decision Records - adr.github.io](https://adr.github.io)
* [The Evolution of Comcast’s Architecture Guild](https://www.infoq.com/articles/architecture-guild-800-friends/)
* [Using Architecture Decision Records – Jonathan Wolski](https://www.youtube.com/watch?v=MQJUWtTM1-E)
* [SATURN 2019 Talk: Creating, Reviewing, and Succeeding with Architectural Decision Records - Ken Power](https://www.youtube.com/watch?v=LFiTwqblqsk)
* [Share the Load: Distribute Design Authority with Architecture Decision Records](https://www.agilealliance.org/resources/experience-reports/distribute-design-authority-with-architecture-decision-records/)
* [List of system quality attributes](https://en.wikipedia.org/wiki/List_of_system_quality_attributes)
* [Decision Log](https://structurizr.com/help/documentation/decision-log)

### ADR Examples
* [Communicating and Documenting Architectural Decisions - David Ayers](https://github.com/davidaayers/comm-and-doc-arch-decisions/blob/master/readme.md)
* [Enterprise Architecture in a Devops Time - David Ayers](https://github.com/davidaayers/ea-talk)
* [Markdown Architectural Decision Records](https://github.com/adr/madr)

### ADR Template Examples
* [Michael Nygard's template](https://github.com/joelparkerhenderson/architecture_decision_record/blob/master/adr_template_by_michael_nygard.md)
* [MADR template](https://adr.github.io/madr/)
* [Ken Power's template](https://youtu.be/LFiTwqblqsk?t=1295)
* [Architecture decision record (ADR) - Joel Parker Henderson](https://github.com/joelparkerhenderson/architecture_decision_record)
