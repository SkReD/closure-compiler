package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.newtypes.JSType;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSTypeNative;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 20.04.13
 * Time: 0:37
 * To change this template use File | Settings | File Templates.
 */
public class InfomaximumCodingConvention extends CodingConventions.Proxy {
	private static final long serialVersionUID = 1L;

	private static final String[] locs = new String[]{"en", "ru"};
	private static final String[] pluralTypes = new String[]{"s", "p1", "p2"};

	/** Decorates a wrapped CodingConvention. */
	public InfomaximumCodingConvention() {
		super(CodingConventions.getDefault());
	}
	public InfomaximumCodingConvention(CodingConvention convention) {
		super(convention);
	}

	/**
	 * Model field keys treated as quoted to avoid conflicts with renamed vars and keys
	 * Also marked as quoted fields in property FIELDS
	 *
	 * Example:
	 *
	 * In this case ID property is treated as quoted
	 * Model.Base.Fields = {
	 *     ID:
	 *     {
	 *         name: 'id'
	 *     }
	 * }
	 * @return boolean
	 */
	public boolean isObjectLiteralKeyTreatAsQuoted(Node objectKey) {
		Node probablyAssignmentNode = objectKey.getParent().getParent();

		if (probablyAssignmentNode.getType() == Token.ASSIGN)
		{
			Node firstChild = probablyAssignmentNode.getFirstChild();
			if (firstChild.getType() == Token.GETPROP)
			{
				String keyString = firstChild.getLastChild().getString();
				return keyString.equals("Fields") || keyString.equals("FIELDS");
			}
		}

		return false;
	}

	public boolean isFunctionSignatureRewriteAllowed(DefinitionSite definitionSite) {
		Node functionNameNode = null;
		DefinitionsRemover.Definition definition = definitionSite.definition;

		if (definition instanceof DefinitionsRemover.AssignmentDefinition) {
			functionNameNode = definitionSite.node.getLastChild();
		} else if (definition instanceof DefinitionsRemover.NamedFunctionDefinition ||
				definition instanceof DefinitionsRemover.VarDefinition) {
			functionNameNode = definitionSite.node;
		}

		return functionNameNode == null || isRenamable(functionNameNode);
	}

	public boolean isRenamable(Node n) throws UnsupportedOperationException{
		String name = n.getString();

		if (name.length() > 2 && (n.isName() || n.isQualifiedName() || n.isStringKey() || n.isFunction() || ((n.isString() && n.getParent().isGetProp()))) &&
				name.matches("^[A-Z_].+") && !name.toUpperCase().equals(name)
				&& !name.matches("^Block|Model|Manager$")
				&& !name.startsWith("View")
				&& !name.endsWith("Set")
				//TODO: Michail Shipov. This condition should be reviewed and removed.
				&& !name.startsWith("On")
				&& !name.equals("Set")
				&& !name.equals("Update")
				&& !name.equals("Remove")
				&& !name.equals("Restore")) {

			return true;
		}

		return false;
	}

	public boolean isLocalization(Node n) {
		if (!n.getString().toUpperCase().equals(n.getString()))
		{
			return isSimpleLocalization(n) || isPluralizeLocalization(n);
		}

		return false;
	}

	//WORD: { 'ru': 'loc', 'en': 'loc' }, где locNode - строка 'loc'
	private boolean isSimpleLocalization(Node locNode)
	{
		Node langNode;
		String lang;
		Boolean result = false;

		langNode = locNode.getParent();
		if (langNode != null && langNode.isStringKey())
		{
			lang = langNode.getString();
			//TODO: Michail Shipov. Move localizations list to additional parameter in options
			for (String loc : locs) {
				if (loc.equals(lang)) {
					result = true;
					break;
				}
			}
		}

		if (result)
		{
			result = isSecondNodeFromLangIsLocalizationId(langNode);
		}

		return result;
	}

	//WORD: { 'en': { s: '', p1: '', p2: '' } }
	private boolean isPluralizeLocalization(Node locNode)
	{
		Node pluralTypeNode, pluralObjectNode;
		String pluralType;
		Boolean result = false;

		pluralTypeNode = locNode.getParent();
		if (pluralTypeNode != null && pluralTypeNode.isStringKey())
		{
			pluralType = pluralTypeNode.getString();
			for (String supportedTypes : pluralTypes) {
				if (pluralType.equals(supportedTypes)) {
					result = true;
					break;
				}
			}
		}

		if (result)
		{
			pluralObjectNode = pluralTypeNode.getParent();
			if (pluralObjectNode != null)
			{
				return isSimpleLocalization(pluralObjectNode);
			}
		}

		return result;
	}

	private boolean isSecondNodeFromLangIsLocalizationId(Node langNode)
	{
		Node locObjectNode, locIdNode = null;

		locObjectNode = langNode.getParent();
		if (locObjectNode != null)
		{
			locIdNode = locObjectNode.getParent();
		}

		if (locIdNode != null && locIdNode.isStringKey())
		{
			if (locIdNode.getString().toUpperCase().equals(locIdNode.getString()))
			{
				return true;
			}
		}

		return false;
	}


	@Override
	public Collection<AssertionFunctionSpec> getAssertionFunctions() {
		return ImmutableList.of(
				new AssertionFunctionSpec("Assert", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertSilent", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertMethod", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertRequestRequiredValue", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertRequiredValue", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertRequiredParameter", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertRequiredParameter", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertInterface", JSType.TRUTHY),
				new AssertionFunctionSpec("AssertComplex", JSType.TRUTHY)
		);
	}
}
