package com.github.emildafinov.ad.sdk.event

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes.Accepted
import com.github.emildafinov.ad.sdk.{EventHandler, EventReturnAddressImpl}
import com.github.emildafinov.ad.sdk.authentication.MarketplaceCredentials
import com.github.emildafinov.ad.sdk.payload.{ApiResult, Event}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

/**
  * Asynchronously handles a raw marketplace event of a particular type.
  *
  * @param transformToClientEvent a function that takes a raw [[Event]] and a string representing the eventId and p
  *                               produces the event instance visible to the client handler
  * @param clientEventHandler     the logic that is being executed from the client connector upon receival of the event]
  * @param toMarketplaceResponse  function that marshals the result of the client event handling into an [ApiResult
  * @tparam A Rich event type
  * @tparam B Result of processing the rich event
  */
class RawEventHandler[A, B](transformToClientEvent: (Event, String) => A,
                            clientEventHandler: EventHandler[A, B],
                            toMarketplaceResponse: B => ApiResult)
                           (implicit appMarketEventResolver: AppMarketEventResolver) {
  
  def processRawEvent(rawEventId: String, rawEvent: Event, clientKey: MarketplaceCredentials)
                     (implicit ec: ExecutionContext): Future[HttpResponse] = Future {

    val clientVisibleEvent = transformToClientEvent(rawEvent, rawEventId)

    Future {
      clientEventHandler.handle(
        clientVisibleEvent, 
        new EventResolutionPromise(
          appMarketEventResolver, 
          toMarketplaceResponse
        )
      )
    }

    HttpResponse(status = Accepted)
  }
}


