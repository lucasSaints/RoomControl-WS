
/*
package br.com.wises.services;

import br.com.wises.database.EManager;
import br.com.wises.database.pojo.AlocacaoSala;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("alocacao")
public class AlocacaoService {
    /*@GET
    @Path("getListaAlocacoes")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<AlocacaoSala> getReunioesById(@HeaderParam("authorization") String autorizacao,@HeaderParam("empresaId") int empId){
       if (authorization != null && authorization.equals("secret")) {
            List<AlocacaoSala> lista = EManager.getInstance().getDbAccessor().getReunioesById(empresaId);
            for (int i = 0; i < lista.size(); i++) {
                lista.get(i).setUsuarioCollection(null);
                lista.get(i).setSalaCollection(null);
            }
            return lista;
        } return null;
    } *
}*/
