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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Locale;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

public class ModelViewerRenderer extends OpenGLLightRenderer
{
	// 定数
	private static final int SIZEOF_BYTE  = Byte.SIZE / 8;    // Byte.SIZEで、byte型のビット数が得られるので、8で割って、バイト数を得る
	private static final int SIZEOF_SHORT = Short.SIZE / 8;    // Short.SIZEで、short型のビット数が得られるので、8で割って、バイト数を得る

	// メンバー変数
	public  boolean     m_bRenderPoint;
	public  boolean     m_bRenderLine;
	public  boolean     m_bRenderFace;
	public  boolean     m_bPickPoint;
	public  boolean     m_bPickLine;
	public  boolean     m_bPickFace;
	private Paint       m_paintMessageTexture;
	private int         m_iMessageTextureID;
	private FloatBuffer m_fbVertexMessageTexture;
	private FloatBuffer m_fbTextureMessageTexture;
	private int[] m_aiVBOidIdColor = new int[2];        // VBO ID

	// コンストラクタ
	public ModelViewerRenderer()
	{
		m_bRenderPoint = true;
		m_bRenderLine = true;
		m_bRenderFace = true;
		m_bPickPoint = true;
		m_bPickLine = true;
		m_bPickFace = true;

		// ペイントの作成
		m_paintMessageTexture = new Paint();
		m_paintMessageTexture.setTextSize( 20 );
		m_paintMessageTexture.setAntiAlias( true );
		m_paintMessageTexture.setColor( Color.WHITE ); // ビットマップにおける文字色は、白。これにより、描画時に、glColorで任意の色付けが可能になる。
		m_paintMessageTexture.setTypeface( Typeface.MONOSPACE ); // 等幅フォント

		// メッセージテクスチャのポリゴン座標値とテクスチャ座標値
		float[] f4Vertex       = { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f };
		float[] f4TextureCoord = { 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f };
		m_fbVertexMessageTexture = makeFloatBuffer( f4Vertex );
		m_fbTextureMessageTexture = makeFloatBuffer( f4TextureCoord );
	}

	// アクセサ
	public int getVBOidVertexIdColor()
	{
		return m_aiVBOidIdColor[0];
	}

	public int getVBOidTriangleIdColor()
	{
		return m_aiVBOidIdColor[1];
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

		if( 0 != model.getVBOidVertex() )
		{ // Use VBO
			// 頂点配列の指定
			gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, model.getVBOidVertex() );
			gl11.glVertexPointer( 3, GL10.GL_FLOAT, 0, 0 );
		}
		else
		{
			// 頂点配列の指定
			gl.glVertexPointer( 3, GL10.GL_FLOAT, 0, model.getVertexBuffer() );
		}

		// 面の描画
		if( m_bRenderFace
			&& null != model.getTriangleVertexIndexBuffer() )
		{
			if( ERenderMode.RM_PICK_ELEMENTID == eRenderMode )
			{
				gl.glEnableClientState( GL10.GL_COLOR_ARRAY );
				if( 0 != getVBOidTriangleIdColor() )
				{ // Use VBO
					// 色配列の指定
					gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, getVBOidTriangleIdColor() );
					gl11.glColorPointer( 4, // Must be 4.
										 GL10.GL_UNSIGNED_BYTE,
										 0,
										 0 );
				}
				else
				{
					// 色配列の指定
					gl.glColorPointer( 4, // Must be 4.
									   GL10.GL_UNSIGNED_BYTE,
									   0,
									   getTriangleIdColorBuffer() );
				}
			}
			else if( ERenderMode.RM_PICK_ELEMENTTYPE == eRenderMode )
			{
				gl.glColor4f( 1.0f, 0.0f, 0.0f, 1.0f );
			}
			else
			{
				gl.glEnableClientState( GL10.GL_NORMAL_ARRAY );
				if( 0 != model.getVBOidNormal() )
				{ // Use VBO
					// 法線ベクトル配列の指定
					gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, model.getVBOidNormal() );
					gl11.glNormalPointer( GL10.GL_FLOAT, 0, 0 );
				}
				else
				{
					// 法線ベクトル配列の指定
					gl.glNormalPointer( GL10.GL_FLOAT, 0, model.getNormalBuffer() );
				}
				gl.glEnable( GL10.GL_LIGHTING ); // 光源の効果を有効にする。
				gl.glEnable( GL10.GL_NORMALIZE ); // OpenGL側で法線ベクトルを単位法線ベクトル化するようにする
				gl.glLightModelx( GL10.GL_LIGHT_MODEL_TWO_SIDE, 1 );// 面の表と裏に光があたるようにする
				gl.glEnable( GL10.GL_COLOR_MATERIAL );// 色設定をマテリアル設定として使用するようにする
				gl.glColor4f( 0.5f, 0.5f, 0.0f, 1.0f );
			}
			if( 0 != model.getVBOidTriangleVertexIndex() )
			{ // Use VBO
				gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, model.getVBOidTriangleVertexIndex() );
				gl11.glDrawElements( GL10.GL_TRIANGLES,
									 model.getTriangleVertexIndexBuffer().capacity(),
									 GL10.GL_UNSIGNED_SHORT,
									 0 );
			}
			else
			{
				gl.glDrawElements( GL10.GL_TRIANGLES,
								   model.getTriangleVertexIndexBuffer().capacity(),
								   GL10.GL_UNSIGNED_SHORT,
								   model.getTriangleVertexIndexBuffer().position( 0 ) );
			}
			gl.glDisable( GL10.GL_LIGHTING ); // 光源の効果を無効にする。
			gl.glDisableClientState( GL10.GL_COLOR_ARRAY );
			gl.glDisableClientState( GL10.GL_NORMAL_ARRAY ); // 法線ベクトル配列の無効化
			// ピック面の描画
			if( ERenderMode.RM_RENDER == eRenderMode )
			{
				if( ERenderElementType.RET_FACE.getValue() == m_aiName[1] )
				{
					int iIndexTriangle = m_aiName[2];
					gl.glColor4f( 1.0f, 1.0f, 0.0f, 1.0f );
					if( 0 != model.getVBOidTriangleVertexIndex() )
					{ // Use VBO
						gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, model.getVBOidTriangleVertexIndex() );
						gl11.glDrawElements( GL10.GL_TRIANGLES,
											 3,
											 GL10.GL_UNSIGNED_SHORT,
											 3 * iIndexTriangle * SIZEOF_SHORT );
					}
					else
					{
						gl.glDrawElements( GL10.GL_TRIANGLES,
										   3,
										   GL10.GL_UNSIGNED_SHORT,
										   model.getTriangleVertexIndexBuffer().position( 3 * iIndexTriangle ) );
					}
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
				if( 0 != model.getVBOidEdgeVertexIndex() )
				{ // Use VBO
					gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, model.getVBOidEdgeVertexIndex() );
				}
				for( iIndexTriangle = 0; iIndexTriangle < iCountTriangle; ++iIndexTriangle )
				{
					for( i3 = 0; i3 < 3; ++i3 )
					{
						iIndexEdge = iIndexTriangle * 3 + i3;
						index2rgb( iIndexEdge, abtRGB );
						gl11.glColor4ub( abtRGB[0], abtRGB[1], abtRGB[2], (byte)255 );
						if( 0 != model.getVBOidEdgeVertexIndex() )
						{ // Use VBO
							gl11.glDrawElements( GL10.GL_LINES,
												 2,
												 GL10.GL_UNSIGNED_SHORT,
												 2 * iIndexEdge * SIZEOF_SHORT );
						}
						else
						{
							gl.glDrawElements( GL10.GL_LINES,
											   2,
											   GL10.GL_UNSIGNED_SHORT,
											   model.getEdgeVertexIndexBuffer().position( 2 * iIndexEdge ) );
						}
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
				if( 0 != model.getVBOidEdgeVertexIndex() )
				{ // Use VBO
					gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, model.getVBOidEdgeVertexIndex() );
					gl11.glDrawElements( GL10.GL_LINES,
										 model.getEdgeVertexIndexBuffer().capacity(),
										 GL10.GL_UNSIGNED_SHORT,
										 0 );
				}
				else
				{
					gl.glDrawElements( GL10.GL_LINES,
									   model.getEdgeVertexIndexBuffer().capacity(),
									   GL10.GL_UNSIGNED_SHORT,
									   model.getEdgeVertexIndexBuffer().position( 0 ) );
				}
			}
			// ピック線の描画
			if( ERenderMode.RM_RENDER == eRenderMode )
			{
				if( ERenderElementType.RET_LINE.getValue() == m_aiName[1] )
				{
					int iIndexEdge = m_aiName[2];
					gl.glLineWidth( 5.0f );
					gl.glColor4f( 0.0f, 1.0f, 1.0f, 1.0f );
					if( 0 != model.getVBOidEdgeVertexIndex() )
					{ // Use VBO
						gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, model.getVBOidEdgeVertexIndex() );
						gl11.glDrawElements( GL10.GL_LINES,
											 2,
											 GL10.GL_UNSIGNED_SHORT,
											 2 * iIndexEdge * SIZEOF_SHORT );
					}
					else
					{
						gl.glDrawElements( GL10.GL_LINES,
										   2,
										   GL10.GL_UNSIGNED_SHORT,
										   model.getEdgeVertexIndexBuffer().position( 2 * iIndexEdge ) );
					}
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
				if( 0 != getVBOidVertexIdColor() )
				{ // Use VBO
					// 色配列の指定
					gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, getVBOidVertexIdColor() );
					gl11.glColorPointer( 4, // Must be 4.
										 GL10.GL_UNSIGNED_BYTE,
										 0,
										 0 );
				}
				else
				{
					// 色配列の指定
					gl.glColorPointer( 4, // Must be 4.
									   GL10.GL_UNSIGNED_BYTE,
									   0,
									   getVertexIdColorBuffer() );
				}
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

		if( 0 != model.getVBOidVertex() )
		{ // Use VBO
			// バインドの解除
			gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
			gl11.glBindBuffer( GL11.GL_ELEMENT_ARRAY_BUFFER, 0 );
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

	// フレームの描画
	@Override
	public void onDrawFrame( GL10 gl )
	{
		long lTimeMillisStart = System.currentTimeMillis();

		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );

		if( !isViewingFrustumValid() )
		{
			setupViewingFrustum();
		}

		if( !isViewingTransformValid() )
		{
			setupViewingTransform();
		}

		preRenderScene();

		gl.glPushMatrix();
		renderStockScene();
		gl.glPopMatrix();

		gl.glPushMatrix();
		renderScene();
		gl.glPopMatrix();

		postRenderScene();

		long lTimeMillisEnd  = System.currentTimeMillis();
		long lTimeMillisDiff = lTimeMillisEnd - lTimeMillisStart;
		if( 0 == lTimeMillisDiff )
		{
			lTimeMillisDiff = 1;
		}
		String strMessage = String.format( Locale.getDefault(), "%5d[fps] ( %6.4f[spf] )", (int)( 1000.0 / lTimeMillisDiff + 0.5 ), lTimeMillisDiff / 1000.0 );

		gl.glPushMatrix();
		renderMessage( gl, strMessage );
		gl.glPopMatrix();
	}

	private void renderMessage( GL10 gl, String strMessage )
	{
		if( 0 == m_iMessageTextureID )
		{
			return;
		}

		// ペイントを使って文字列を描画した場合の文字列のピクセル幅、ピクセル高さの取得
		int iStringWidth = (int)( m_paintMessageTexture.measureText( strMessage ) + 1.0f );
		iStringWidth = ( iStringWidth + 31 ) & ( ~31 ); // 32の倍数に
		Paint.FontMetrics fontmetrics   = m_paintMessageTexture.getFontMetrics();
		int               iStringHeight = (int)( fontmetrics.bottom - fontmetrics.top + 1.0f );
		iStringHeight = ( iStringHeight + 31 ) & ( ~31 ); // 32の倍数に

		// 文字列テクスチャの作成
		// 空のビットマップ作成
		Bitmap bitmap = Bitmap.createBitmap( iStringWidth, iStringHeight, Bitmap.Config.ARGB_8888 );
		// キャンバスをビットマップの上に作成
		Canvas canvas = new Canvas( bitmap );
		// ビットマップを透明色で塗りつぶす
		bitmap.eraseColor( Color.TRANSPARENT );

		// テキストをペイントでキャンバスに描画
		canvas.drawText( strMessage, 0, -fontmetrics.top, m_paintMessageTexture );

		// 作成したテクスチャをバインド
		gl.glBindTexture( GL10.GL_TEXTURE_2D, m_iMessageTextureID );

		// 拡大縮小の方法
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST ); // 縮小は、もっとも近い要素で補完
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR ); // 拡大は、線形で補完

		// 繰り返しの方法
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE ); // 画像の端のピクセルの色を使う
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE ); // 画像の端のピクセルの色を使う

		// GLUtils.texImage2Dを使ってglTexImage2Dを呼び出す。
		GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );

		// ビットマップ用メモリはもう用なし
		bitmap.recycle();

		// OpenGLのテクスチャ描画処理

		// エクスチャＯＮ
		gl.glEnable( GL10.GL_TEXTURE_2D );

		// 透過処理の有効化
		gl.glEnable( GL10.GL_ALPHA_TEST );
		gl.glEnable( GL10.GL_BLEND );
		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		gl.glTexEnvf( GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE );

		// その他の機能の有効化、無効化
		gl.glDisable( GL10.GL_DEPTH_TEST );
		gl.glDisable( GL10.GL_LIGHTING );
		gl.glDisable( GL10.GL_CULL_FACE );

		// 変換行列作成
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof( 0.0f, getWidth(), getHeight(), 0.0f, -1.0f, 1.0f ); // 画面の左上隅が(0,0) になり、右下隅が(2,2)になる視野角錐台。
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glPushMatrix();
		gl.glLoadIdentity();

		// テクスチャを貼るポリゴンの座標値の更新
		m_fbVertexMessageTexture.put( 0, 0.0f );
		m_fbVertexMessageTexture.put( 1, getHeight() - (float)iStringHeight );
		m_fbVertexMessageTexture.put( 2, (float)iStringWidth );
		m_fbVertexMessageTexture.put( 3, getHeight() - (float)iStringHeight );
		m_fbVertexMessageTexture.put( 4, 0.0f );
		m_fbVertexMessageTexture.put( 5, getHeight() );
		m_fbVertexMessageTexture.put( 6, (float)iStringWidth );
		m_fbVertexMessageTexture.put( 7, getHeight() );

		gl.glEnableClientState( GL10.GL_VERTEX_ARRAY );
		gl.glEnableClientState( GL10.GL_TEXTURE_COORD_ARRAY );

		gl.glVertexPointer( 2, GL10.GL_FLOAT, 0, m_fbVertexMessageTexture );
		gl.glTexCoordPointer( 2, GL10.GL_FLOAT, 0, m_fbTextureMessageTexture );

		gl.glColor4f( 1.0f, 1.0f, 0.0f, 1.0f );
		gl.glDrawArrays( GL10.GL_TRIANGLE_STRIP, 0, 4 );

		gl.glDisableClientState( GL10.GL_VERTEX_ARRAY );
		gl.glDisableClientState( GL10.GL_TEXTURE_COORD_ARRAY );

		// 変換行列を元に戻す
		gl.glPopMatrix();
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glPopMatrix();
		gl.glMatrixMode( GL10.GL_MODELVIEW );

		// 機能の有効化、無効化の復元
		gl.glEnable( GL10.GL_DEPTH_TEST );
		gl.glDisable( GL10.GL_ALPHA_TEST );
		gl.glDisable( GL10.GL_BLEND );
		gl.glDisable( GL10.GL_TEXTURE_2D );
	}

	public void createMessageTexture()
	{
		destroyMessageTexture();

		GL10  gl            = getGL();
		int[] aiTextureName = new int[1];
		gl.glGenTextures( 1, aiTextureName, 0 );
		m_iMessageTextureID = aiTextureName[0];
	}

	public void destroyMessageTexture()
	{
		if( 0 == m_iMessageTextureID )
		{
			return;
		}

		GL10  gl            = getGL();
		int[] aiTextureName = { m_iMessageTextureID };
		gl.glDeleteTextures( 1, aiTextureName, 0 );
		m_iMessageTextureID = 0;
	}

	@Override
	public void preSurfaceDestroy()
	{
		destroyVBO();
		destroyMessageTexture();

		super.preSurfaceDestroy();
	}

	@Override
	public void onSurfaceCreated( GL10 gl, EGLConfig config )
	{
		super.onSurfaceCreated( gl, config );

		createMessageTexture();
		createVBO();
	}

	@Override
	public void setModel( Model model )
	{
		// VBOの破棄処理
		destroyVBO();

		super.setModel( model );

		// VBOの構築処理
		createVBO();
	}

	public void createVBO()
	{
		destroyVBO();

		if( !( getGL() instanceof GL11 ) )
		{
			return;
		}
		GL11 gl11 = (GL11)getGL();

		// モデルに関するVBO
		if( null != getModel() )
		{
			getModel().createVBO( getGL() );
		}

		// ピック処理用の要素番号色に関するVBO
		if( null != getModel() )
		{    // ピック処理用の要素番号色は、モデルと連動しているので、モデルがない場合は、VBO作成スキップ
			// VBO ID 配列の生成
			gl11.glGenBuffers( m_aiVBOidIdColor.length, m_aiVBOidIdColor, 0 );
			// データの転送
			if( 0 != m_aiVBOidIdColor[0] )
			{
				// 頂点番号色データの転送
				gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, getVBOidVertexIdColor() );
				gl11.glBufferData( GL11.GL_ARRAY_BUFFER, getVertexIdColorBuffer().capacity() * SIZEOF_BYTE, getVertexIdColorBuffer(), GL11.GL_STATIC_DRAW );

				// 三角形番号色データの転送
				gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, getVBOidTriangleIdColor() );
				gl11.glBufferData( GL11.GL_ARRAY_BUFFER, getTriangleIdColorBuffer().capacity() * SIZEOF_BYTE, getTriangleIdColorBuffer(), GL11.GL_STATIC_DRAW );

				// バインドの解除
				gl11.glBindBuffer( GL11.GL_ARRAY_BUFFER, 0 );
			}
		}
	}

	public void destroyVBO()
	{
		if( !( getGL() instanceof GL11 ) )
		{
			return;
		}
		GL11 gl11 = (GL11)getGL();

		// ピック処理用の要素番号色に関するVBO
		if( 0 != m_aiVBOidIdColor[0] )
		{
			gl11.glDeleteBuffers( m_aiVBOidIdColor.length, m_aiVBOidIdColor, 0 );
			Arrays.fill( m_aiVBOidIdColor, 0 );
		}

		// モデルに関するVBO
		if( null != getModel() )
		{
			getModel().destroyVBO( getGL() );
		}
	}
}

