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

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

public class StlFileLoader
{
	public static Model load( String strPath )
	{
		File file = new File( strPath );
		if( 0 == file.length() )
		{
			return null;
		}

		// ファーストパース（要素数カウント）
		int[] aiCountTriangle = new int[1];
		if( !parse_first( strPath, aiCountTriangle ) )
		{
			return null;
		}

		// 領域確保
		if( 0 == aiCountTriangle[0] )
		{
			return null;
		}
		float[] af3Vertex = new float[aiCountTriangle[0] * 3 * 3];

		// セカンドパース（値詰め）
		if( !parse_second( strPath, af3Vertex ) )
		{
			return null;
		}

		//return new Model( af3Vertex );

		// モデルデータ化
		float[] f4Ambient = { 0.25f, 0.20725f, 0.20725f, 1.0f };
		float[] f4Diffuse = { 1.0f, 0.829f, 0.829f, 1.0f };
		float[] f4Specular = { 0.296648f, 0.296648f, 0.296648f, 1.0f };
		float fShininess = 0.088f;
		Material material = new Material(f4Ambient, f4Diffuse, f4Specular, fShininess);
		Group[] aGroup = new Group[1];
		aGroup[0] = new Group( "", af3Vertex, null, material );
		return new Model( aGroup );
	}

	private static boolean parse_first( String strPath, int[] aiCountTriangle )
	{
		// インプットのチェック
		if( null == aiCountTriangle )
		{
			return false;
		}
		// アウトプットの初期化
		aiCountTriangle[0] = 0;

		try
		{
			BufferedReader br             = new BufferedReader( new FileReader( strPath ) );
			int            iIndexTriangle = 0;
			while( true )
			{
				String strReadString = br.readLine();
				if( null == strReadString )
				{
					break;
				}
				StringTokenizer stReadString = new StringTokenizer( strReadString, ", \t\r\n" );
				if( !stReadString.hasMoreTokens() )
				{
					continue;
				}
				String token = stReadString.nextToken();
				if( token.equalsIgnoreCase( "endfacet" ) )
				{
					++iIndexTriangle;
					continue;
				}
			}
			br.close();
			aiCountTriangle[0] = iIndexTriangle;
			return true;
		}
		catch( Exception e )
		{
			Log.e( "StlFileLoader", "parse_first error : " + e );
			return false;
		}
	}

	private static boolean parse_second( String strPath, float[] af3Vertex )
	{
		// インプットのチェック
		if( null == af3Vertex )
		{
			return false;
		}
		int iIndexTriangle = 0;
		int iIndex3        = 0;

		try
		{
			BufferedReader br = new BufferedReader( new FileReader( strPath ) );
			while( true )
			{
				String strReadString = br.readLine();
				if( null == strReadString )
				{
					break;
				}
				StringTokenizer stReadString = new StringTokenizer( strReadString, ", \t\r\n" );
				if( !stReadString.hasMoreTokens() )
				{
					continue;
				}
				String token = stReadString.nextToken();
				if( token.equalsIgnoreCase( "vertex" ) )
				{
					if( 3 <= iIndex3 )
					{
						continue;
					}
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 0] = Float.valueOf( stReadString.nextToken() );
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 1] = Float.valueOf( stReadString.nextToken() );
					af3Vertex[iIndexTriangle * 9 + iIndex3 * 3 + 2] = Float.valueOf( stReadString.nextToken() );
					++iIndex3;
					continue;
				}
				else if( token.equalsIgnoreCase( "facet" ) )
				{ // 面法線ベクトル
					iIndex3 = 0;
					continue;
				}
				else if( token.equalsIgnoreCase( "endfacet" ) )
				{
					++iIndexTriangle;
					continue;
				}
				else if( token.equalsIgnoreCase( "solid" ) )
				{ // ソリッド名
					continue;
				}
			}
			br.close();
			return true;
		}
		catch( Exception e )
		{
			Log.e( "StlFileLoader", "parse_second error : " + e );
			return false;
		}
	}
}

