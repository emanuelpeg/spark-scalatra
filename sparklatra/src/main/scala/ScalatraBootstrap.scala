import com.hexacta.app.input._
import com.hexacta.app.example.MyScalatraServlet
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new MyScalatraServlet, "/example/*")
    context.mount(new InputServlet, "/updateDataBase/*")
  }
}
