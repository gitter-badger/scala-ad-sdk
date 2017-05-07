package com.github.emildafinov.ad.sdk.event

import com.github.emildafinov.ad.sdk.authentication.AppMarketCredentialsImpl
import com.github.emildafinov.ad.sdk.event.responses.marshallers.EventResponseMarshaller
import com.github.emildafinov.ad.sdk.http.client.AppMarketEventResolver
import com.github.emildafinov.ad.sdk.payload.{ApiResult, ApiResults}
import com.github.emildafinov.ad.sdk.{EventReturnAddressImpl, UnitTestSpec}
import org.mockito.Matchers.{any, eq => mockEq}
import org.mockito.Mockito.{verify, when}
import org.mockito.{ArgumentCaptor, Mockito}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SdkProvidedEventResolverTest extends UnitTestSpec {

  behavior of "SdkProvidedEventResolver"
  private val appMarketEventResolverMock = mock[AppMarketEventResolver]
  private val testResolutionHost = "http://example.com"
  private val testEventId = "eventId"
  private val testCredentials = AppMarketCredentialsImpl(
    clientKey = "clientKey",
    clientSecret = "clientSecret"
  )
  private val testReturnAddress = new EventReturnAddressImpl(
    testEventId,
    testResolutionHost,
    testCredentials.clientKey
  )
  private val testEventResponse = "testEventResponse"

  before {
    Mockito.reset(appMarketEventResolverMock)
  }

  it should "call the underlying event resolver with a failure payload" in {
    //Given
    val expectedPayloadSent = ApiResults.failure("")
    val mockMarshaller = new EventResponseMarshaller[String] {
      override def convertToAppMarketResponse(eventResponse: String): ApiResult = expectedPayloadSent
    }
    val testErrorMessage = "errorMessage"
    val returnMessageCaptor = ArgumentCaptor.forClass(classOf[ApiResult])

    when {
      appMarketEventResolverMock.sendEventResolvedCallback(any(), any())
    } thenReturn {
      Future {
        System.out.print("mock execution")
      }
    }

    val tested = new SdkProvidedEventResolver[String](appMarketEventResolverMock, mockMarshaller)

    //When
    tested.resolveWithFailure(testErrorMessage, testReturnAddress)

    //Then
    verify(appMarketEventResolverMock)
      .sendEventResolvedCallback(
        mockEq(testReturnAddress),
        returnMessageCaptor.capture()
      )

    returnMessageCaptor.getValue.success shouldEqual false
  }

  it should "call the underlying event resolver with a success payload" in {
    //Given
    val expectedPayloadSent = ApiResults.success()
    val mockMarshaller = new EventResponseMarshaller[String] {
      override def convertToAppMarketResponse(eventResponse: String): ApiResult = expectedPayloadSent
    }
    val returnMessageCaptor = ArgumentCaptor.forClass(classOf[ApiResult])

    when {
      appMarketEventResolverMock.sendEventResolvedCallback(any(), any())
    } thenReturn {
      Future {
        System.out.print("mock execution")
      }
    }
    val tested = new SdkProvidedEventResolver[String](appMarketEventResolverMock, mockMarshaller)

    //When
    tested.resolveSuccessfully(testEventResponse, testReturnAddress)

    //Then
    verify(appMarketEventResolverMock)
      .sendEventResolvedCallback(
        mockEq(testReturnAddress),
        returnMessageCaptor.capture()
      )

    returnMessageCaptor.getValue shouldEqual expectedPayloadSent
  }
}
