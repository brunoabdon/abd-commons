import javax.ws.rs.GET;
import javax.ws.rs.Path;

public abstract class AbstractResource 
                        <E extends ParamInterface<P>, P> {

    @GET
    @Path("{id}")
    public void pegar() {

    }
}