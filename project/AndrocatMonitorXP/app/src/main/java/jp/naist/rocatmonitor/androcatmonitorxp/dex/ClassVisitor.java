package jp.naist.rocatmonitor.androcatmonitorxp.dex;

import com.googlecode.d2j.Method;
import com.googlecode.d2j.visitors.DexClassVisitor;
import com.googlecode.d2j.visitors.DexMethodVisitor;

import jp.naist.rocatmonitor.androcatmonitorxp.Monitor;

public class ClassVisitor extends DexClassVisitor
{

  public String ClassName = null;

  @Override
  public DexMethodVisitor visitMethod(int accessFlags, Method method)
  {
    Monitor.getInstance().StructureDB.registMethod(ClassName, method.getName());
    return null;
  }

};

