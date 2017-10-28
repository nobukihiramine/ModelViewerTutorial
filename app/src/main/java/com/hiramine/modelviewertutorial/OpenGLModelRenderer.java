/*
 * Copyright 2017 Nobuki HIRAMINE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hiramine.modelviewertutorial;

import javax.microedition.khronos.opengles.GL10;

public class OpenGLModelRenderer extends OpenGLTrackRenderer
{
	// メンバー変数
	private Model m_model;

	// アクセサ
	public Model getModel()
	{
		return m_model;
	}

	public void setModel( Model model )
	{
		m_model = model;
	}

	@Override
	protected void renderScene()
	{
		renderModel();
	}

	protected void renderModel()
	{
		if( null == m_model
			|| null == m_model.getVertexBuffer() )
		{
			return;
		}

		GL10 gl = getGL();

		// 頂点配列の有効化
		gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );

		// 頂点配列の指定
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, m_model.getVertexBuffer() );

		// 面の描画
		if( null != m_model.getTriangleVertexIndexBuffer() )
		{
			gl.glColor4f( 0.5f, 0.5f, 0.0f, 1.0f );
			gl.glDrawElements( GL10.GL_TRIANGLES,
							   m_model.getTriangleVertexIndexBuffer().capacity(),
							   GL10.GL_UNSIGNED_SHORT,
							   m_model.getTriangleVertexIndexBuffer().position( 0 ) );
		}

		// 線の描画
		if( null != m_model.getEdgeVertexIndexBuffer() )
		{
			gl.glLineWidth( 2.0f );
			gl.glColor4f( 0.0f, 0.5f, 0.5f, 1.0f );
			gl.glDrawElements( GL10.GL_LINES,
							   m_model.getEdgeVertexIndexBuffer().capacity(),
							   GL10.GL_UNSIGNED_SHORT,
							   m_model.getEdgeVertexIndexBuffer().position( 0 ) );
		}

		// 点の描画
		// if( null != m_model.getVertexBuffer() )
		{
			gl.glPointSize( 5.0f );
			gl.glColor4f( 0.5f, 0.0f, 0.5f, 1.0f );
			gl.glDrawArrays( GL10.GL_POINTS, 0, m_model.getVertexCount() );
		}

		// 頂点配列の無効化
		gl.glDisableClientState( GL10.GL_VERTEX_ARRAY );
	}
}
