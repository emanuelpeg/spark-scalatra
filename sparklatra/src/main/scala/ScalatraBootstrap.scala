import com.hexacta.app.input._
import com.hexacta.app.example.MyScalatraServlet
import com.hexacta.app.output.GitHubServlet
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    //Este es el ejemplo
    context.mount(new MyScalatraServlet, "/example/*")
    //Este es el importador de datos
    context.mount(new InputServlet, "/updateDataBase/*")
    //Consultas sobre los datos de GitHub en HBase
    context.mount(new GitHubServlet, "/github/*")
  }
}