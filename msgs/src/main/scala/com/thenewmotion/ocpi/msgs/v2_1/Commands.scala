package com.thenewmotion.ocpi.msgs
package v2_1

import java.time.ZonedDateTime
import java.util.UUID

import Locations.{ConnectorId, EvseUid, LocationId}
import Sessions.SessionId
import Tokens.Token
import com.thenewmotion.ocpi.{Enumerable, Nameable}

object Commands {

  sealed abstract class CommandName(val name: String) extends Nameable
  object CommandName extends Enumerable[CommandName] {
    case object ReserveNow extends CommandName("RESERVE_NOW")
    case object StartSession extends CommandName("START_SESSION")
    case object StopSession extends CommandName("STOP_SESSION")
    case object UnlockConnector extends CommandName("UNLOCK_CONNECTOR")
    val values = Iterable(ReserveNow, StartSession, StopSession, UnlockConnector)
  }

  abstract class Command(val name: CommandName) {
    def responseUrl: Url
  }

  private[ocpi] def callbackUrl(
    baseUrl: Url,
    cmdId: UUID
  ): Url = baseUrl / cmdId.toString

  object Command {
    case class ReserveNow(
      responseUrl: Url,
      token: Token,
      expiryDate: ZonedDateTime,
      reservationId: Int,
      locationId: LocationId,
      evseUid: Option[EvseUid]
    ) extends Command(CommandName.ReserveNow)

    object ReserveNow {
      def apply(
        baseUrl: Url,
        commandId: UUID,
        token: Token,
        expiryDate: ZonedDateTime,
        reservationId: Int,
        locationId: LocationId,
        evseUid: Option[EvseUid]
      ): ReserveNow =
        ReserveNow(
          callbackUrl(baseUrl, commandId),
          token,
          expiryDate,
          reservationId,
          locationId,
          evseUid
        )
    }

    case class StartSession(
      responseUrl: Url,
      token: Token,
      locationId: LocationId,
      evseUid: Option[EvseUid]
    ) extends Command(CommandName.StartSession)

    object StartSession {
      def apply(
        baseUrl: Url,
        commandId: UUID,
        token: Token,
        locationId: LocationId,
        evseUid: Option[EvseUid]
      ): StartSession =
        StartSession(
          callbackUrl(baseUrl, commandId),
          token,
          locationId,
          evseUid
        )
    }

    case class StopSession(
      responseUrl: Url,
      sessionId: SessionId
    ) extends Command(CommandName.StopSession)

    object StopSession {
      def apply(
        baseUrl: Url,
        commandId: UUID,
        sessionId: SessionId
      ): StopSession =
        StopSession(
        callbackUrl(baseUrl, commandId),
        sessionId
      )
    }

    case class UnlockConnector(
      responseUrl: Url,
      locationId: LocationId,
      evseUid: EvseUid,
      connectorId: ConnectorId
    ) extends Command(CommandName.UnlockConnector)

    object UnlockConnector {
      def apply(
        baseUrl: Url,
        commandId: UUID,
        locationId: LocationId,
        evseUid: EvseUid,
        connectorId: ConnectorId
      ): UnlockConnector =
        UnlockConnector(
          callbackUrl(baseUrl, commandId),
          locationId, evseUid, connectorId
        )
    }
  }

  sealed abstract class CommandResponseType(val name: String) extends Nameable
  implicit object CommandResponseType extends Enumerable[CommandResponseType] {
    case object NotSupported extends CommandResponseType("NOT_SUPPORTED")
    case object Rejected extends CommandResponseType("REJECTED")
    case object Accepted extends CommandResponseType("ACCEPTED")
    case object Timeout extends CommandResponseType("TIMEOUT")
    case object UnknownSession extends CommandResponseType("UNKNOWN_SESSION")
    val values = Iterable(NotSupported, Rejected, Accepted, Timeout, UnknownSession)
  }

  case class CommandResponse(result: CommandResponseType)
}
