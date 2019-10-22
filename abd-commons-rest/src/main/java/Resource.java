import javax.ws.rs.DELETE;
import javax.ws.rs.Path;

@Path("{id}")
public class Resource <E extends ParamInterface<P>, P> {

    @DELETE
    public void pegar() {

    }
}