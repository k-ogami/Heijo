package rocatmonitor;

import java.util.Objects;

public class MethodInfoKey
{
  public String ClassSig;
  public String MethodSig;
  public String MethodName;

  public MethodInfoKey(String classSig, String methodSig, String methodName)
  {
    ClassSig = classSig;
    MethodSig = methodSig;
    MethodName = methodName;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj instanceof MethodInfoKey) {
      MethodInfoKey key = (MethodInfoKey)obj;
      return this.ClassSig.equals(key.ClassSig) && this.MethodSig.equals(key.MethodSig) && this.MethodName.equals(key.MethodName);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(ClassSig, MethodSig, MethodName);
  }

}
