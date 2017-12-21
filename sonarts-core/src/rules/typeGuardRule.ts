/*
 * SonarTS
 * Copyright (C) 2017-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import * as tslint from "tslint";
import * as ts from "typescript";
import { SonarRuleMetaData } from "../sonarRule";
import { functionLikeMainToken, is } from "../utils/navigation";
import { isParenthesizedExpression } from "typescript";

export class Rule extends tslint.Rules.AbstractRule {
  public static metadata: SonarRuleMetaData = {
    ruleName: "type-guard",
    description: "...",
    rspecKey: "RSPEC-XXXX",
    type: "maintainability",
    typescriptOnly: false,
    optionsDescription: "",
    options: null,
  };

  public apply(sourceFile: ts.SourceFile): tslint.RuleFailure[] {
    return this.applyWithWalker(new Walker(sourceFile, this.getOptions()));
  }
}

class Walker extends tslint.RuleWalker {

  public visitNode(node: ts.Node) {
    if (ts.isFunctionDeclaration(node) || ts.isMethodDeclaration(node)) {
      const { parameters, body } = node;
      const returnExpression = this.returnExpression(body);
      if (parameters.length == 1 && returnExpression) {
        if (ts.isBinaryExpression(returnExpression) && is(returnExpression.operatorToken, ts.SyntaxKind.ExclamationEqualsEqualsToken, ts.SyntaxKind.ExclamationEqualsToken)) {
          const {left, right} = returnExpression;
          if (this.isUndefined(right) && this.isPropertyOfCasted(left)) {
            this.addFailureAtNode(functionLikeMainToken(node), "Add type guard");
          }
        } else if (this.isNegation(returnExpression) && this.isNegation(returnExpression.operand) && this.isPropertyOfCasted(returnExpression.operand.operand)) {
          this.addFailureAtNode(functionLikeMainToken(node), "Add type guard");
        }
      }
    }

    super.visitNode(node);
  }

  private isNegation(node: ts.Expression): node is ts.PrefixUnaryExpression {
    return ts.isPrefixUnaryExpression(node) && node.operator == ts.SyntaxKind.ExclamationToken;
  }

  private isPropertyOfCasted(node: ts.Expression) {
    if (isParenthesizedExpression(node)) {
      node = node.expression;
    }

    if (is(node, ts.SyntaxKind.PropertyAccessExpression)) {
      let obj: ts.Expression = (node as ts.PropertyAccessExpression).expression;
      if (ts.isParenthesizedExpression(obj)) {
        obj = obj.expression;
      }

      return is(obj, ts.SyntaxKind.AsExpression, ts.SyntaxKind.TypeAssertionExpression);
    }

    return false;
  }

  private isUndefined(node: ts.Expression) {
    return ts.isIdentifier(node) && node.text === "undefined";
  }

  private returnExpression(body?: ts.Block | ts.Expression): ts.Expression | undefined {
    if (body) {
      if (ts.isBlock(body)) {
        if (body.statements.length == 1 && ts.isReturnStatement(body.statements[0])) {
          return (body.statements[0] as ts.ReturnStatement).expression;
        }

      } else {
        // arrow function expression body
        return body;
      }
    }
  }


}
