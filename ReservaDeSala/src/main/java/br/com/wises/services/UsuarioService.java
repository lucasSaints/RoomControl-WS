package br.com.wises.services;

import br.com.wises.database.EManager;
import br.com.wises.database.pojo.Organizacao;
import br.com.wises.database.pojo.Status;
import br.com.wises.database.pojo.Usuario;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.json.JSONObject;

@Path("usuario")
public class UsuarioService {

// Esse aqui fica só de exemplo pra mostrar como faz pra pegar o parâmetro pelo path do request
//    @GET
//    @Path("/{email}")
//    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
//    public Usuario getUserJson(
//            @PathParam("email") String email,
//            @HeaderParam("authorization") String authorization) {
//        if (authorization != null && authorization.equals("secret")) {
//            Usuario user = EManager.getInstance().getDbAccessor().getUserByEmail(email);
//            if (user != null) {
//                user.getIdOrganizacao().setUsuarioCollection(null);
//                user.getIdOrganizacao().setSalaCollection(null);
//                user.setSenha(null);
//                return user;
//            }
//        } else {
//            return null;
//        }
//        return null;
//    }
    @GET
    @Path("getByEmail")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Usuario getUserByEmailJson(
            @HeaderParam("email") String email,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getUserByEmail(email);
            if (user != null) {
                //user.setSenha(null);
                return user;
            }
        } else {
            return null;
        }
        return null;
    }

    @GET
    @Path("getById")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Usuario getUserByIdJson(
            @HeaderParam("id") int id,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getUserById(id);
            if (user != null) {
                return user;
            }
        } else {
            return null;
        }
        return null;
    }

    @GET
    @Path("login")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String authentication(
            @HeaderParam("email") String email,
            @HeaderParam("password") String password,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getCredencials(email, password);
            if (user != null) {
                if (user.isAtivo()) {
                    return "Login efetuado com sucesso!";
                } else {
                    return "Usuário bloqueado";
                }
            } else {
                return "Credenciais Inválidas!";
            }
        } else {
            return "Token Inválido";
        }
    }

    @GET
    @Path("loginV2")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Usuario authenticationV2(
            @HeaderParam("email") String email,
            @HeaderParam("password") String password,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getCredencials(email, password);
            if (user != null) {
                user.setSenha(null);
                return user;
            }
        } else {
            return null;
        }
        return null;
    }

    @GET
    @Path("loginRodrigao")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response authenticationovo(
            @HeaderParam("email") String email,
            @HeaderParam("password") String password,
            @HeaderParam("authorization") String authorization) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getCredencials(email, password);
            if (user != null) {
                user.setSenha(null);
                return Response.ok(user).header("Access-Control-Allow-Origin", "*")
                        .header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
                        .allow("OPTIONS").build();
            } else {
                user = EManager.getInstance().getDbAccessor().getUserByEmail(email);
                if (user != null) {
                    return Response.ok(user).entity(new Status("Senha incorreta"))
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                } else {
                    return Response.ok(user).entity(new Status("Usuário não encontrado"))
                            .type(MediaType.APPLICATION_JSON)
                            .build();
                }
            }
        } else {
            return Response
                    .status(Response.Status.FORBIDDEN)
                    .entity(new Status("Request inválido"))
                    .build();
        }
    }

    @GET
    @Path("checkAdmin")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Boolean checarSeEhAdm(@HeaderParam("authorization") String authorization, @HeaderParam("id") int id) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getUserById(id);
            return user.isAdmin();
        } else {
            return false;
        }
    }

    @GET
    @Path("checkBoss")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Boolean checarSeEhChefe(@HeaderParam("authorization") String authorization, @HeaderParam("id") int id) {
        if (authorization != null && authorization.equals("secret")) {
            Usuario user = EManager.getInstance().getDbAccessor().getUserById(id);
            List<Usuario> users = EManager.getInstance().getDbAccessor().getUsersByEmpresa(user.getIdOrganizacao().getId());
            for (int i = 0; i < users.size(); i++) {
                if (users.get(i).getId() < user.getId()) {
                    return false;
                }
            }
            return true;
        } else {
            return null;
        }
    }

    @GET
    @Path("getByEmpresa")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Usuario> getUsersByEmp(@HeaderParam("authorization") String authorization, @HeaderParam("idEmp") int idEmp) {
        if (authorization != null && authorization.equals("secret")) {
            List<Usuario> users = EManager.getInstance().getDbAccessor().getUsersByEmpresa(idEmp);
            return users;
        } else {
            return null;
        }
    }

    @GET
    @Path("getAdminIds")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public List<Integer> getAdminList(@HeaderParam("authorization") String authorization, @HeaderParam("idEmp") int idEmp) {
        if (authorization.equals("secret")) {
            List<Integer> lista = new ArrayList<>();
            List<Usuario> users = EManager.getInstance().getDbAccessor().getUsersByEmpresa(idEmp);
            for (Usuario u : users) {
                if (u.isAdmin()) {
                    lista.add(u.getId());
                }
            }
            return lista;
        } else {
            return null;
        }
    }

    @POST
    @Path("remover")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String demissao(@HeaderParam("authorization") String authorization, @HeaderParam("id") int id) {
        if (authorization.equals("secret")) {
            if (!checarSeEhChefe("secret", id)) {
                EManager.getInstance().getDbAccessor().removeUser(id);
                return "200. Membro removido com sucesso\n\n";
            } else {
                return "403. Não autorizado.";
            }
        } else {
            return "401. Token inválido";
        }
    }

    @POST
    @Path("promover")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String promocao(@HeaderParam("authorization") String authorization, @HeaderParam("id") int id) {
        if (authorization.equals("secret")) {
            EManager.getInstance().getDbAccessor().promoteUser(id);
            return "200. Membro promovido com sucesso\n\n";
        } else {
            return "403. Token inválido";
        }
    }

    @POST
    @Path("cadastro")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String cadastrarUsuario(@HeaderParam("authorization") String authorization,
            @HeaderParam("novoUsuario") String novoUsuarioEncoded) {
        if (authorization != null && authorization.equals("secret")) {
            try {
                //String userEncodedOk = "ewogICAgImVtYWlsIjogInJvZHJpZ28ucXVpc2VuQHdpc2VzLmNvbS5iciIsCiAgICAibm9tZSI6ICJSb2RyaWdvIFF1aXNlbiAzIiwKICAgICJzZW5oYSI6ICIxMjMiCn0=";
                //String userEncodedNotOk = "ewogICAgImVtYWlsIjogInJvZHJpZ28ucXVpc2VuQHdpc2UuY29tLmJyIiwKICAgICJub21lIjogIlJvZHJpZ28gUXVpc2VuIDUiLAogICAgInNlbmhhIjogIjEyMyIKfQ==";
                String userDecoded = new String(Base64.getDecoder().decode(novoUsuarioEncoded.getBytes()), Charset.forName("UTF-8"));

                JSONObject userObj = new JSONObject(userDecoded);
                Usuario novoUsuario = new Usuario();
                String email, nome, senha;
                String dominio = null;
                int idOrganizacao = 0;

                if (userObj.has("email") && userObj.has("nome") && userObj.has("senha") && userObj.has("idOrganizacao")) {
                    email = userObj.getString("email");
                    nome = userObj.getString("nome");
                    senha = userObj.getString("senha");
                    idOrganizacao = userObj.getInt("idOrganizacao");

                    if (email.isEmpty() || nome.isEmpty() || senha.isEmpty() || idOrganizacao == 0) {
                        return "Erro ao criar conta, os dados enviados estão incompletos";
                    } else if (email.contains("@")) {
                        dominio = email.split("@")[1];
                    }
                } else {
                    return "Erro ao criar conta, os dados enviados estão incompletos";
                }

                if (EManager.getInstance().getDbAccessor().getUserByEmail(email) != null) {
                    return "O email informado já está cadastrado";
                }

                Organizacao organizacao = new Organizacao();
                try {
                    organizacao = EManager.getInstance().getDbAccessor().getOrganizacaoById(idOrganizacao);
                    if (organizacao == null) {
                        return "Erro ao cadastrar usuário, a organização informada não existe";
                    }
                } catch (Exception e) {
                    return "Erro ao criar conta, os dados enviados estão incompletos";
                }

                novoUsuario.setEmail(email);
                novoUsuario.setNome(nome);
                novoUsuario.setSenha(senha);
                novoUsuario.setAtivo(true);
                novoUsuario.setIdOrganizacao(organizacao);

                EManager.getInstance().getDbAccessor().novoUsuario(novoUsuario);

                return "Usuário criado com sucesso";
            } catch (Exception e) {
                e.printStackTrace();
                return "Erro ao criar usuário";
            }

        } else {
            return "Token inválido";
        }
    }
}
