package se.miun.dt002g.webscraper.gui;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class NodeUtilities
{
	public static String getXPath(Node node)
	{
		StringBuilder builder = new StringBuilder();
		Stack<String> stack = new Stack<>();

		Node currNode = node;
		while (currNode.getParentNode() != null)
		{
			List<Node> nodeList = filterNodes(currNode.getParentNode().getChildNodes(), currNode.getNodeName());
			if (nodeList.size() > 1)
			{
				for (int i = 0; i < nodeList.size(); i++)
				{
					if (currNode.isSameNode(nodeList.get(i)))
					{
						stack.push(currNode.getNodeName().toLowerCase() + "[" + (i+1) + "]");
						break;
					}
				}
			}
			else stack.push(currNode.getNodeName().toLowerCase());

			currNode = currNode.getParentNode();
		}

		while (!stack.isEmpty())
		{
			builder.append("/").append(stack.pop());
		}

		return builder.toString();
	}

	public static List<Node> filterNodes(NodeList list, String name)
	{
		List<Node> newList = new ArrayList<>();

		for (int i = 0; i < list.getLength(); i++)
		{
			if (list.item(i).getNodeName().equals(name)) newList.add(list.item(i));
		}

		return newList;
	}

	public static void addOnclick(Node node, EventListener listener)
	{
		((EventTarget)node).addEventListener("click", listener, true);
	}
}
