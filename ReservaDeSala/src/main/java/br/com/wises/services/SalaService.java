package br.com.wises.services;

import br.com.wises.database.EManager;
import br.com.wises.database.pojo.Organizacao;
import br.com.wises.database.pojo.Sala;
import java.nio.charset.Charset;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONObject;

@Path("sala")
public class SalaService {

    @GET
    @Path("salas")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Sala> getSalas(
            @HeaderParam("id_organizacao") int idOrganizacao,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            List<Sala> lista = EManager.getInstance().getDbAccessor().getSalasByOrganizacaoId(idOrganizacao);
            return lista;
        } else {
            return null;
        }
    }
    
    @GET
    @Path("getById")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Sala getSala(@HeaderParam("id") int id,@HeaderParam("authorization") String authorization){
        if (authorization != null && authorization.equals("secret")) {
            Sala sala = EManager.getInstance().getDbAccessor().getSalaById(id);
            return sala;
        }else{
            return null;
        }
    }
    
    @POST
    @Path("salvar")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String salvarSala(@HeaderParam("jsonSala") String novaSalaEncoded,@HeaderParam("authorization") String authorization){
        if (authorization != null && authorization.equals("secret")) {
            JSONObject jsonSala = new JSONObject(new String(Base64.getDecoder().decode(novaSalaEncoded.getBytes()), Charset.forName("UTF-8")));
            if(jsonSala.has("nome")&&jsonSala.has("localizacao")){
                Sala newSala=new Sala();
                newSala.setNome(jsonSala.getString("nome"));
                newSala.setLocalizacao(jsonSala.getString("localizacao"));
                if(jsonSala.has("quantPCs"))
                    newSala.setQuantPCs(jsonSala.getInt("quantPCs"));
                else
                    newSala.setQuantPCs(0);
                if(jsonSala.has("quantidadePessoasSentadas"))
                    newSala.setQuantidadePessoasSentadas(jsonSala.getInt("quantidadePessoasSentadas"));
                else
                    newSala.setQuantidadePessoasSentadas(0);
                if(jsonSala.has("possuiMultimidia"))
                    newSala.setPossuiMultimidia(jsonSala.getBoolean("possuiMultimidia"));
                else
                    newSala.setPossuiMultimidia(false);
                if(jsonSala.has("possuiArcon"))
                    newSala.setPossuiArcon(jsonSala.getBoolean("possuiArcon"));
                else
                    newSala.setPossuiArcon(false);
                newSala.setAtivo(true);
                Organizacao org=new Organizacao();
                org.setId(              jsonSala.getJSONObject("idOrganizacao").getInt("id"));
                org.setNome(            jsonSala.getJSONObject("idOrganizacao").getString("nome"));
                org.setDominio(         jsonSala.getJSONObject("idOrganizacao").getString("dominio"));
                if(jsonSala.getJSONObject("idOrganizacao").has("endereco"))
                    org.setEndereco(        jsonSala.getJSONObject("idOrganizacao").getString("endereco"));
                org.setTipoOrganizacao( jsonSala.getJSONObject("idOrganizacao").getString("tipoOrganizacao").charAt(0));
                if(org.getTipoOrganizacao()=='F')
                    org.setIdOrganizacaoPai(jsonSala.getJSONObject("idOrganizacao").getInt("idOrganizacaoPai"));
                org.setAtivo(true);
                newSala.setIdOrganizacao(org);
                Date date = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(c.HOUR, -3);
                date = c.getTime();
                newSala.setDataCriacao(date);
                newSala.setDataAlteracao(date);

                EManager.getInstance().getDbAccessor().novaSala(newSala);
                return "sucesso!";
            }else
                return "JSON incompleto";
        }else
            return "Token inválido";
    }
}
