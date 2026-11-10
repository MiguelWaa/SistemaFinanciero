import com.sun.source.tree.*;
import com.sun.source.util.JavacTask;
import javax.tools.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Catálogo reproducible desde el AST Java. No ejecuta código del proyecto. JDK 17+. */
public class GenerarCatalogoJava {
    static String q(String s) { return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t")+"\""; }
    public static void main(String[] args) throws Exception {
        Path root=Path.of(args.length>0?args[0]:".").toAbsolutePath().normalize();
        Path out=root.resolve("docs/modelos/Catalogo_clases.json");Files.createDirectories(out.getParent());
        JavaCompiler compiler=ToolProvider.getSystemJavaCompiler();
        if(compiler==null)throw new IllegalStateException("Se requiere un JDK completo");
        List<Path> files;try(var walk=Files.walk(root.resolve("src"))){files=walk.filter(p->p.toString().endsWith(".java")).sorted().toList();}
        List<String> types=new ArrayList<>();
        try(var fm=compiler.getStandardFileManager(null,null,StandardCharsets.UTF_8)) {
            JavacTask task=(JavacTask)compiler.getTask(null,fm,null,List.of("-proc:none"),null,fm.getJavaFileObjectsFromPaths(files));
            for(CompilationUnitTree unit:task.parse()) {
                String pkg=String.valueOf(unit.getPackageName());
                String file=root.relativize(Path.of(unit.getSourceFile().toUri())).toString().replace('\\','/');
                for(Tree tree:unit.getTypeDecls()) if(tree instanceof ClassTree c) {
                    List<String> fields=new ArrayList<>(),methods=new ArrayList<>(),relations=new ArrayList<>();
                    for(Tree member:c.getMembers()) {
                        if(member instanceof VariableTree v) fields.add("{\"nombre\":"+q(v.getName().toString())+",\"tipo\":"+q(String.valueOf(v.getType()))+",\"modificadores\":"+q(v.getModifiers().getFlags().toString())+"}");
                        if(member instanceof MethodTree m) methods.add(q(m.getModifiers().getFlags()+" "+(m.getReturnType()==null?"":m.getReturnType()+" ")+(m.getName().contentEquals("<init>")?c.getSimpleName():m.getName())+"("+m.getParameters()+")"));
                    }
                    if(c.getExtendsClause()!=null)relations.add(q("extends "+c.getExtendsClause()));
                    for(Tree x:c.getImplementsClause())relations.add(q("implements "+x));
                    types.add("{\"clase\":"+q(pkg+"."+c.getSimpleName())+",\"categoria\":"+q(c.getKind().toString())+",\"archivo\":"+q(file)+",\"atributos\":["+String.join(",",fields)+"],\"metodos\":["+String.join(",",methods)+"],\"herencia\":["+String.join(",",relations)+"]}");
                }
            }
        }
        Files.writeString(out,"[\n"+String.join(",\n",types)+"\n]\n");System.out.println(types.size()+" tipos Java documentados en "+out);
    }
}
