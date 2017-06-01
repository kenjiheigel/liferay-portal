/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.poshi.runner;

import static com.liferay.poshi.runner.ReadableSyntaxKeys.AND;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.BACKGROUND;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.FEATURE;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.GIVEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.SCENARIO;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.SETUP;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.TEARDOWN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THEN;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.THESE_PROPERTIES;
import static com.liferay.poshi.runner.ReadableSyntaxKeys.WHEN;
import static com.liferay.poshi.runner.util.StringPool.COLON;
import static com.liferay.poshi.runner.util.StringPool.PIPE;
import static com.liferay.poshi.runner.util.StringPool.TAB;

import com.liferay.poshi.runner.util.Dom4JUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;

/**
 * @author Kenji Heigel
 */
public abstract class BasePoshiElement implements PoshiElement {

	public BasePoshiElement(Element element) {
		this(element, null);
	}

	public BasePoshiElement(Element element, PoshiElement parentElement) {
		_parentElement = parentElement;

		addAttributes(element);
		addChildElements(element);
		setTagName(element);
	}

	public BasePoshiElement(String readableSyntax, PoshiElement parentElement) {
		_parentElement = parentElement;

		addAttributes(readableSyntax);
		addChildElements(readableSyntax);
		setTagName();
	}

	@Override
	public void addAttributes(Element element) {
		for (Iterator i = element.attributeIterator(); i.hasNext();) {
			Attribute attribute = (Attribute)i.next();

			attributes.put(attribute.getName(), attribute.getValue());
		}
	}

	@Override
	public void addAttributes(String readableSyntax) {
	}

	@Override
	public void addChildElements(Element element) {
		for (Iterator i = element.elementIterator(); i.hasNext();) {
			PoshiElement poshiElement = PoshiElementFactory.newPoshiElement(
				(Element)i.next(), this);

			if (poshiElement != null) {
				_childElements.add(poshiElement);
			}
		}
	}

	@Override
	public void addChildElements(String readableSyntax) {
	}

	@Override
	public List<PoshiElement> getChildElements() {
		return _childElements;
	}

	public List<PoshiElement> getChildElements(String tag) {
		List<PoshiElement> childElements = new ArrayList<>();

		for (PoshiElement childElement : getChildElements()) {
			String childElementTagName = childElement.getTagName();

			if (childElementTagName.equals(tag)) {
				childElements.add(childElement);
			}
		}

		return childElements;
	}

	@Override
	public PoshiElement getParentElement() {
		return _parentElement;
	}

	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public String toReadableSyntax() {
		StringBuilder sb = new StringBuilder();

		for (PoshiElement childElement : _childElements) {
			sb.append(childElement.toReadableSyntax());
		}

		return sb.toString();
	}

	@Override
	public Element toXML() {
		Element element = Dom4JUtil.getNewElement(tagName);

		for (String key : attributes.keySet()) {
			element.addAttribute(key, attributes.get(key));
		}

		for (PoshiElement childElement : _childElements) {
			Dom4JUtil.addToElement(element, childElement.toXML());
		}

		return element;
	}

	protected int getIndex() {
		List<PoshiElement> siblingElements =
			getParentElement().getChildElements();

		return siblingElements.indexOf(this);
	}

	protected String getReadableTitle() {
		int index = getIndex();

		if (index == 0) {
			return GIVEN;
		}
		else if (index == (getSiblingElementsSize() - 1)) {
			return THEN;
		}

		return AND;
	}

	protected int getSiblingElementsSize() {
		List<PoshiElement> siblingElements =
			getParentElement().getChildElements();

		return siblingElements.size();
	}

	protected void setTagName() {
	}

	protected void setTagName(Element element) {
		tagName = element.getName();
	}

	protected static final String[] READABLE_COMMAND_BLOCK_KEYS = {
		BACKGROUND + COLON, FEATURE + COLON, SCENARIO + COLON, SETUP + COLON,
		TEARDOWN + COLON
	};

	protected static final String[] READABLE_EXECUTE_BLOCK_KEYS = {
		TAB + AND, TAB + GIVEN, TAB + THEN, TAB + WHEN
	};

	protected static final String[] READABLE_VARIABLE_BLOCK_KEYS = {
		TAB + TAB + PIPE
	};

	protected Map<String, String> attributes = new HashMap<>();
	protected String tagName;

	private final List<PoshiElement> _childElements = new ArrayList<>();
	private final PoshiElement _parentElement;

}