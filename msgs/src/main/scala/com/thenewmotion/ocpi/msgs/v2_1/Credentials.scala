package com.thenewmotion.ocpi.msgs
package v2_1

import CommonTypes.BusinessDetails

object Credentials {

  case class Creds[O <: Ownership](
    token: AuthToken[O],
    url: Url,
    businessDetails: BusinessDetails,
    partyId: PartyId,
    countryCode: CountryCode
  )
}

