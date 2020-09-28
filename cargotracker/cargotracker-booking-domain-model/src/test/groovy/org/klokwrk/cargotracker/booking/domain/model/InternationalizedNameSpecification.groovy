package org.klokwrk.cargotracker.booking.domain.model

import spock.lang.Shared
import spock.lang.Specification

class InternationalizedNameSpecification extends Specification {

  @SuppressWarnings("SpellCheckingInspection")
  @Shared
  String funkyString = "Tĥïŝ ĩš â fůňķŷ Šťŕĭńġ"

  @SuppressWarnings("SpellCheckingInspection")
  @Shared
  String funkyStringInternationalized = "This is a funky String"

  void "map constructor should work for correct input params"() {
    when:
    InternationalizedName internationalizedName = new InternationalizedName(name: nameParameter)

    then:
    internationalizedName.name == nameParameter

    where:
    nameParameter | _
    "a"           | _
    "Zagreb"      | _
    "Baška"       | _
    "Baška"       | _
    funkyString   | _
  }

  void "map constructor should fail for invalid input params"() {
    when:
    new InternationalizedName(name: nameParameter)

    then:
    thrown(AssertionError)

    where:
    nameParameter | _
    null          | _
    ""            | _
    "   "         | _
  }

  void "getNameInternationalized() should return expected value"() {
    when:
    InternationalizedName internationalizedName = new InternationalizedName(name: nameParameter)

    then:
    internationalizedName.nameInternationalized == nameInternationalizedParameter

    where:
    nameParameter | nameInternationalizedParameter
    "a"           | "a"
    "Zagreb"      | "Zagreb"
    "Baška"       | "Baska"
    //noinspection SpellCheckingInspection
    "čćžšđČĆŽŠĐ"  | "cczsdCCZSD"
    funkyString   | funkyStringInternationalized
  }
}
