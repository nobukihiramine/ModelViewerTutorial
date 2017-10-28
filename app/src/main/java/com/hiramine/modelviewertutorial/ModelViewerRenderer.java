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
import javax.microedition.khronos.opengles.GL11;

public class ModelViewerRenderer extends OpenGLPickRenderer
{
	// メンバー変数
	public boolean m_bRenderPoint;
	public boolean m_bRenderLine;
	public boolean m_bRenderFace;
	public boolean m_bPickPoint;
	public boolean m_bPickLine;
	public boolean m_bPickFace;

	// コンストラクタ
	public ModelViewerRenderer()
	{
		m_bRenderPoint = true;
		m_bRenderLine = true;
		m_bRenderFace = true;
		m_bPickPoint = true;
		m_bPickLine = true;
		m_bPickFace = true;
	}

	@Override
	protected void renderModel( ERenderMode eRenderMode )
	{
		Model model = getModel();
		if( null == model
			|| null == model.getVertexBuffer() )
		{
			return;
		}

		GL10 gl = getGL();

		if( !( gl instanceof GL11 ) )
		{
			return;
		}
		GL11 gl11 = (GL11)gl;

		// 頂点配列の有効化
		gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );

		// 頂点配列の指定
		gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, model.getVertexBuffer() );

		// 面の描画
		if( m_bRenderFace
			&& null != model.getTriangleVertexIndexBuffer() )
		{
			if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
			{
				gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
				gl.glColorPointer( 4, // Must be 4.
								   GL10.GL_UNSIGNED_BYTE,
								   0,
								   getTriangleIdColorBuffer() );
			}
			else if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
			{
				gl.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
			}
			else
			{
				gl.glColor4f( 0.5f, 0.5f, 0.0f, 1.0f );
			}
			gl.glDrawElements( GL10.GL_TRIANGLES,
							   model.getTriangleVertexIndexBuffer().capacity(),
							   GL10.GL_UNSIGNED_SHORT,
							   model.getTriangleVertexIndexBuffer().position( 0 ) );
			gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
			// ピック面の描画
			if( ERenderMode.RM_RENDER == eRenderMode )
			{
				if( ERenderElementType.RET_FACE.getValue() == m_aiName[1] )
				{
					int iIndexTriangle = m_aiName[2];
					gl.glColor4f( 1.0f, 1.0f, 0.0f, 1.0f );
					gl.glDrawElements( GL10.GL_TRIANGLES,
									   3,
									   GL10.GL_UNSIGNED_SHORT,
									   model.getTriangleVertexIndexBuffer().position( 3 * iIndexTriangle ) );
				}
			}
		}

		// 線の描画
		if( m_bRenderLine
			&& null != model.getEdgeVertexIndexBuffer() )
		{
			gl.glLineWidth( 2.0f );
			if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
			{
				int    iCountTriangle = model.getTriangleCount();
				int    iIndexTriangle;
				int    i3;
				int    iIndexEdge;
				byte[] abtRGB         = { 0, 0, 0 };
				for( iIndexTriangle = 0; iIndexTriangle < iCountTriangle; ++iIndexTriangle )
				{
					for( i3 = 0; i3 < 3; ++i3 )
					{
						iIndexEdge = iIndexTriangle * 3 + i3;
						index2rgb( iIndexEdge, abtRGB );
						gl11.glColor4ub( abtRGB[0], abtRGB[1], abtRGB[2], (byte)255 );
						gl.glDrawElements( GL10.GL_LINES,
										   2,
										   GL10.GL_UNSIGNED_SHORT,
										   model.getEdgeVertexIndexBuffer().position( 2 * iIndexEdge ) );
					}
				}
			}
			else
			{
				if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
				{
					gl.glColor4f( 0.0f, 1.0f, 0.0f, 1.0f );
				}
				else
				{
					gl.glColor4f( 0.0f, 0.5f, 0.5f, 1.0f );
				}
				gl.glDrawElements( GL10.GL_LINES,
								   model.getEdgeVertexIndexBuffer().capacity(),
								   GL10.GL_UNSIGNED_SHORT,
								   model.getEdgeVertexIndexBuffer().position( 0 ) );
			}
			// ピック線の描画
			if( ERenderMode.RM_RENDER == eRenderMode )
			{
				if( ERenderElementType.RET_LINE.getValue() == m_aiName[1] )
				{
					int iIndexEdge = m_aiName[2];
					gl.glLineWidth( 5.0f );
					gl.glColor4f( 0.0f, 1.0f, 1.0f, 1.0f );
					gl.glDrawElements( GL10.GL_LINES,
									   2,
									   GL10.GL_UNSIGNED_SHORT,
									   model.getEdgeVertexIndexBuffer().position( 2 * iIndexEdge ) );
				}
			}
		}

		// 点の描画
		if( m_bRenderPoint )
		// && null != model.getVertexBuffer() )
		{
			gl.glPointSize( 5.0f );
			if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
			{
				gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
				gl.glColorPointer( 4, // Must be 4.
								   GL10.GL_UNSIGNED_BYTE,
								   0,
								   getVertexIdColorBuffer() );
			}
			else if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
			{
				gl.glColor4f( 0.0f, 0.0f, 1.0f, 1.0f );
			}
			else
			{
				gl.glColor4f( 0.5f, 0.0f, 0.5f, 1.0f );
			}
			gl.glDrawArrays( GL10.GL_POINTS, 0, model.getVertexCount() );
			gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
			// ピック点の描画
			if( ERenderMode.RM_RENDER == eRenderMode )
			{
				if( ERenderElementType.RET_POINT.getValue() == m_aiName[1] )
				{
					gl.glPointSize( 10.0f );
					gl.glColor4f( 1.0f, 0.0f, 1.0f, 1.0f );
					int iIndexPoint = m_aiName[2];
					gl.glDrawArrays( GL10.GL_POINTS, iIndexPoint, 1 );
				}
			}
		}

		// 頂点配列の無効化
		gl.glDisableClientState( GL10.GL_VERTEX_ARRAY );
	}

	@Override
	protected void DecidePickNameArray( int[][] aaiName )
	{
		int  iId_selected          = -1;
		int  iElementType_selected = ERenderElementType.RET_FACE.getValue() + 1;
		long lSquareDist_selected  = ( 2 + PICKREGIONOFFSET ) * ( 2 + PICKREGIONOFFSET ) + ( 2 + PICKREGIONOFFSET ) * ( 2 + PICKREGIONOFFSET );
		long lSquareDist_current;
		int  x;
		int  y;

		for( int i = 0; i < aaiName.length; ++i )
		{
			if( 0 == aaiName[i][1] )
			{ // モデルの外側
				continue;
			}

			if( !m_bPickPoint
				&& ERenderElementType.RET_POINT.getValue() == aaiName[i][1] )
			{ // 点ピックOFFの場合は、点はピックできない
				continue;
			}
			if( !m_bPickLine
				&& ERenderElementType.RET_LINE.getValue() == aaiName[i][1] )
			{ // 線ピックOFFの場合は、線はピックできない
				continue;
			}
			if( !m_bPickFace
				&& ERenderElementType.RET_FACE.getValue() == aaiName[i][1] )
			{ // 面ピックOFFの場合は、面はピックできない
				continue;
			}

			if( iElementType_selected < aaiName[i][1] )
			{ // 要素タイプ的に、優先順位が低い
				continue;
			}

			if( iElementType_selected > aaiName[i][1] )
			{ // 要素タイプ的に、優先順位が低い
				iId_selected = i;
				iElementType_selected = aaiName[i][1];
				x = i % ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
				y = i / ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
				lSquareDist_selected = x * x + y * y;
				continue;
			}

			// 要素タイプ的に、優先順位が同じ場合は、ピック領域の中心に近いものが優先度が高い。
			x = i % ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
			y = i / ( 1 + 2 * PICKREGIONOFFSET ) - PICKREGIONOFFSET;
			lSquareDist_current = x * x + y * y;
			if( lSquareDist_selected > lSquareDist_current )
			{
				iId_selected = i;
				iElementType_selected = aaiName[i][1];
				lSquareDist_selected = lSquareDist_current;
				continue;
			}
		}

		if( -1 != iId_selected )
		{ // 名前列メンバの更新
			System.arraycopy( aaiName[iId_selected], 0, m_aiName, 0, NAMEARRAYSIZE );
		}
	}
}

