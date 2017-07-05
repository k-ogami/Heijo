using UnityEngine;
using System.Collections.Generic;
using System;

public class BinPackingMaker
{

  private class Node
  {
    public float Width = 0;
    public float Height = 0;
    public float X = 0;
    public float Y = 0;
    public Node Right = null;
    public Node Down = null;
    public CityObject Object = null;
    public bool IsUsed = false;

    // デバッグ用
    public override string ToString()
    {
      return Object + "(" + Width + "," + Height + ")";
    }
  }

  private Dictionary<long, Node> fitDict = new Dictionary<long, Node>();

  private const float FLOAT_THREASHOLD = 0.001f;

  public void Make()
  {
    Node root = RecPlace(Manager.CityObjectDB.DefaultPackage);
    fitDict.Add(Manager.CityObjectDB.DefaultPackage.ID, root);
    RecSetPos(root);
  }

  private Node RecPlace(CityObject obj)
  {
    Node node = new Node();
    node.Object = obj;
    node.IsUsed = true;

    if (obj.IsMethod) {
      node.Width = node.Height = Manager.CityMaker.MethodWidth + Manager.CityMaker.EdgeWidth * 2;
      obj.WidthX = obj.WidthZ = Manager.CityMaker.MethodWidth + Manager.CityMaker.EdgeWidth * 2;
    }
    else {
      List<Node> children = new List<Node>();
      foreach (CityObject child in obj.GetChildren()) {
        children.Add(RecPlace(child));
      }
      Node inside = MakeInside(children);
      node.Width = obj.WidthX = inside.Width + Manager.CityMaker.EdgeWidth * 2;
      node.Height = obj.WidthZ = inside.Height + Manager.CityMaker.EdgeWidth * 2;
    }

    return node;
  }

  private Node MakeInside(List<Node> children)
  {
    children.Sort(CompareByArea);
    Node root = new Node();
    root.Width = children.Count > 0 ? children[0].Width : 0;
    root.Height = children.Count > 0 ? children[0].Height : 0;

    foreach (Node child in children) {
      Node node = FindNode(root, child.Width, child.Height);
      Node fit = null;
      if (node != null) {
        fit = SplitNode(node, child.Width, child.Height);
      }
      else {
        fit = GrowNode(ref root, child.Width, child.Height);
      }
      fitDict.Add(child.Object.ID, fit);
    }

    return root;
  }

  private int CompareByArea(Node a, Node b)
  {
    float areaA = a.Width * a.Height;
    float areaB = b.Width * b.Height;
    if (areaA == areaB) {
      return 0;
    }
    else if (areaA < areaB) {
      return 1;
    }
    else {
      return -1;
    }
  }

  private Node FindNode(Node root, float width, float height)
  {
    if (root.IsUsed) {
      Node findRight = FindNode(root.Right, width, height);
      if (findRight != null) return findRight;
      Node findDown = FindNode(root.Down, width, height);
      if (findDown != null) return findDown;
      return null;
    }
    else if ((width <= root.Width + FLOAT_THREASHOLD) && (height <= root.Height + FLOAT_THREASHOLD)) {
      return root;
    }
    else {
      return null;
    }
  }

  private Node SplitNode(Node node, float width, float height)
  {
    node.IsUsed = true;

    node.Right = new Node()
    {
      X = node.X + width,
      Y = node.Y,
      Width = node.Width - width,
      Height = height
    };

    node.Down = new Node()
    {
      X = node.X,
      Y = node.Y + height,
      Width = node.Width,
      Height = node.Height - height
    };

    return node;
  }

  private Node GrowNode(ref Node root, float width, float height)
  {
    bool canGrowRight = (height <= root.Height + FLOAT_THREASHOLD);
    bool canGrowDown = (width <= root.Width + FLOAT_THREASHOLD);

    bool shouldGrowRight = canGrowRight && (root.Height + FLOAT_THREASHOLD >= (root.Width + width));
    bool shouldGrowDown = canGrowDown && (root.Width + FLOAT_THREASHOLD >= (root.Height + height));

    if (shouldGrowRight) {
      return GrowRight(ref root, width, height);
    }
    else if (shouldGrowDown) {
      return GrowDown(ref root, width, height);
    }
    else if (canGrowRight) {
      return GrowRight(ref root, width, height);
    }
    else if (canGrowDown) {
      return GrowDown(ref root, width, height);
    }
    else {
      return null;
    }
  }

  private Node GrowRight(ref Node root, float width, float height)
  {
    root = new Node()
    {
      IsUsed = true,
      X = 0,
      Y = 0,
      Width = root.Width + width,
      Height = root.Height,
      Right = new Node()
      {
        X = root.Width,
        Y = 0,
        Width = width,
        Height = root.Height
      },
      Down = root
    };
    return SplitNode(root.Right, width, height);
  }

  private Node GrowDown(ref Node root, float width, float height)
  {
    root = new Node()
    {
      IsUsed = true,
      X = 0,
      Y = 0,
      Width = root.Width,
      Height = root.Height + height,
      Right = root,
      Down = new Node()
      {
        X = 0,
        Y = root.Height,
        Width = root.Width,
        Height = height
      }
    };
    return SplitNode(root.Down, width, height);
  }

  private void RecSetPos(Node _node)
  {
    foreach (var pair in fitDict) {
      CityObject obj = Manager.CityObjectDB.ObjectDict[pair.Key];
      Node node = pair.Value;
      obj.transform.position += new Vector3(node.X + obj.WidthX / 2, 0, node.Y + obj.WidthZ / 2);
      AddPosToChildren(obj, new Vector3(Manager.CityMaker.EdgeWidth + node.X, 0, Manager.CityMaker.EdgeWidth + node.Y));
      obj.transform.localScale = new Vector3(obj.WidthX - Manager.CityMaker.EdgeWidth * 2, 1, obj.WidthZ - Manager.CityMaker.EdgeWidth * 2);
    }
  }

  private void AddPosToChildren(CityObject obj, Vector3 pos, bool parent = true)
  {
    if (!parent) obj.transform.position += pos;
    foreach (CityObject child in obj.GetChildren()) {
      AddPosToChildren(child, pos, false);
    }
  }

}
