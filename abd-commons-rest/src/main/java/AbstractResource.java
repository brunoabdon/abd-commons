import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("{id}")
public class AbstractResource 
                        <E extends ParamInterface<P>, P> {

    @GET
    public void pegar() {

    }
}