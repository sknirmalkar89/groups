package org.sunbird.actors;

import static org.powermock.api.mockito.PowerMockito.when;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import java.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.message.Localizer;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Localizer.class)
public class HealthActorTest extends BaseActorTest {

  @Test
  public void testIt() {
    PowerMockito.mockStatic(Localizer.class);
    when(Localizer.getInstance()).thenReturn(null);
    new TestKit(system) {
      {
        final Props props = Props.create(HealthActor.class);
        final ActorRef subject = system.actorOf(props);
        final TestKit probe = new TestKit(system);
        Request reqObj = new Request();
        reqObj.setHeaders(headerMap);
        reqObj.setOperation("healthy");
        subject.tell(reqObj, getRef());
        Response response = expectMsgClass(Duration.ofSeconds(10), Response.class);
        Assert.assertTrue(null != response);
      }
    };
  }
}
