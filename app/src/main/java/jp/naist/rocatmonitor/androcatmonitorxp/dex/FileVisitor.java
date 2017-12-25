package jp.naist.rocatmonitor.androcatmonitorxp.dex;

import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexFileVisitor;

import jp.naist.rocatmonitor.androcatmonitorxp.ConstValue;
import jp.naist.rocatmonitor.androcatmonitorxp.Monitor;

public class FileVisitor extends DexFileVisitor
{

  final public ClassVisitor cv;

  public FileVisitor(ClassVisitor cv)
  {
    this.cv = cv;
  }

  @Override
  public DexClassVisitor visit(int access_flags, final String classSig, String superClass, String[] interfaceNames)
  {
    String packageName = getPackageNameFromClassSig(classSig);
    if (Monitor.getInstance().StructureDB.isIgnorePackage(packageName)) return null;
    cv.ClassName = getClassNameFromClassSig(classSig);
    return cv;
  }

  private String getPackageNameFromClassSig(String classSig)
  {
    // "Lcom/example/Hoge;" -> "com.example"

    String fullname = classSig.substring(1, classSig.length() - 1);
    String[] tokens = fullname.split("/");

    if (tokens.length == 0) {
      return null;
    }
    if (tokens.length == 1) {
      return ConstValue.DEFAULT_PACKAGE_NAME;
    }
    else {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < tokens.length - 1; i++) {
        if (i != 0) builder.append(".");
        builder.append(tokens[i]);
      }
      return builder.toString();
    }
  }

  private String getClassNameFromClassSig(String classSig)
  {
    // "Lcom/example/Hoge;" -> "com.example.Hoge"

    String fullname = classSig.substring(1, classSig.length() - 1);
    String[] tokens = fullname.split("/");

    if (tokens.length == 0) {
      return null;
    }
    else {
      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < tokens.length; i++) {
        if (i != 0) builder.append(".");
        builder.append(tokens[i]);
      }
      return builder.toString();
    }
  }

};
