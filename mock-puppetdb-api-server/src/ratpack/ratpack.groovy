import static ratpack.groovy.Groovy.ratpack
import ratpack.server.BaseDir

ratpack {
    handlers {


        fileSystem "assets", { f ->
            f.files()

//            { ctx->
//                ctx.getResponse().getHeaders().add("Content-Type", "application/json");
//            }
        }
    }

}