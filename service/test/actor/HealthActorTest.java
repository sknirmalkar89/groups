package actor;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import java.time.Duration;
import org.junit.Assert;
import org.junit.Test;
import org.sunbird.HealthActor;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

public class HealthActorTest extends BaseActorTest {

  @Test
  public void testIt() {
    new TestKit(system) {
      {
        final Props props = Props.create(HealthActor.class);
        final ActorRef subject = system.actorOf(props);
        final TestKit probe = new TestKit(system);
        Request reqObj = new Request();
        reqObj.setOperation("healthy");
        subject.tell(reqObj, getRef());
        Response response = expectMsgClass(Duration.ofSeconds(10), Response.class);
        Assert.assertTrue(null != response);
      }
    };
  }
}
