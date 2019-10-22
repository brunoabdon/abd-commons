import javax.ws.rs.GET;
import javax.ws.rs.Path;

public abstract class AbstractRestReadOnlyResource 
                        <E extends Identifiable<Chave>, Chave> {

    @GET
    @Path("{id}")
    public void pegar() {

    }
}