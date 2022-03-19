package se.miun.dt002g.webscraper.gui;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Contains utility functions for Nodes.
 */
public class NodeUtilities
{
	/**
	 * Get the xPath for a given Node.
	 * @param node The Node for which you want the path of.
	 * @return The xPath to the Node.
	 */
	public static String getXPath(Node node)
	{
		StringBuilder builder = new StringBuilder();
		Stack<String> stack = new Stack<>();

		Node currNode = node;
		while (currNode.getParentNode() != null) // While the current node is not the topmost node.
		{
			// Get all sibling nodes with the same element as the current node.
			List<Node> nodeList = filterNodes(currNode.getParentNode().getChildNodes(), currNode.getNodeName());
			if (nodeList.size() > 1) // If there are more than one sibling node with the same element in the current node.
			{
				for (int i = 0; i < nodeList.size(); i++)
				{
					// Loop through all sibling nodes with the same element as the current node.
					// If the node in the list is the same as the current node, push it onto the stack with the index number.
					if (currNode.isSameNode(nodeList.get(i)))
					{
						stack.push(currNode.getNodeName().toLowerCase() + "[" + (i+1) + "]");
						break;
					}
				}
			}
			else stack.push(currNode.getNodeName().toLowerCase()); // If the node is the only one with the same element, push it onto the stack without index number.

			currNode = currNode.getParentNode(); // Move one step up the node tree.
		}

		while (!stack.isEmpty()) // Loop through the stack and add each node to the path string.
		{
			builder.append("/").append(stack.pop());
		}

		return builder.toString();
	}

	/**
	 * Filters nodes and returns a list with only nodes of the same element.
	 * @param list A list with nodes.
	 * @param name The name of the element to filter by.
	 * @return A list containing only nodes of the same element as name.
	 */
	public static List<Node> filterNodes(NodeList list, String name)
	{
		List<Node> newList = new ArrayList<>();

		for (int i = 0; i < list.getLength(); i++)
		{
			if (list.item(i).getNodeName().equals(name)) newList.add(list.item(i));
		}

		return newList;
	}

	/**
	 * Adds an onClickListener to a node.
	 * @param node The node to add the listener to.
	 * @param listener The listener.
	 */
	public static void addOnclick(Node node, EventListener listener)
	{
		((EventTarget)node).addEventListener("click", listener, true);
	}
}
