package developmentpermission.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.ColumnValue;
import developmentpermission.entity.Distance;
import developmentpermission.entity.Oid;
import developmentpermission.entity.RoadCenterLinePosition;
import developmentpermission.entity.RoadLod2;
import developmentpermission.entity.SpiltLineExtent;
import developmentpermission.entity.SplitLine;
import developmentpermission.entity.SplitRoadCenterLine;

/**
 * F_レイヤDAO
 */
@Transactional
public class JudgementLayerDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(JudgementLayerDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public JudgementLayerDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 指定レイヤと指定地番図形が重なるか判定
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param applicationId   申請ID
	 * @param targetTableName 比較テーブル名
	 * @return 比較テーブル内図形で地番と重なった図形のOID1件
	 */
	@SuppressWarnings("unchecked")
	public List<Oid> getIntersectsOid(List<Integer> lotNumberIdList, Integer applicationId, String targetTableName) {
		LOGGER.debug("指定レイヤと指定地番図形が重なるか判定 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String getLotNumberQuery = "";
			if (applicationId == null) {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    geom " + //
						"  FROM " + //
						"    f_lot_number " + //
						"  WHERE " + //
						"    chiban_id IN (:lotNumbers) ";
			} else {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    geom " + //
						"  FROM " + //
						"    f_application_lot_number " + //
						"  WHERE " + //
						"    application_id = :applicationId ";
			}
			String sql = "" + //
					"WITH lon_number_geom AS ( " + // 地番の特定
					getLotNumberQuery + //
					") " + //
					"SELECT DISTINCT " + //
					"  a.ogc_fid AS oid " + //
					"FROM " + //
					"  " + targetTableName + " AS a " + //
					"INNER JOIN " + //
					"  lon_number_geom AS b " + // 接触確認
					"ON " + //
					"  ST_Intersects(a.wkb_geometry, b.geom) " + //
					"ORDER BY a.ogc_fid ASC";
			if (applicationId == null) {
				return em.createNativeQuery(sql, Oid.class).setParameter("lotNumbers", lotNumberIdList).getResultList();
			} else {
				return em.createNativeQuery(sql, Oid.class).setParameter("applicationId", applicationId)
						.getResultList();
			}

		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定レイヤと指定地番図形が重なるか判定 終了");
		}
	}

	/**
	 * 指定レイヤと指定地番図形が重なるか判定
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param applicationId   申請ID
	 * @param targetTableName 比較テーブル名
	 * @return 比較テーブル内図形で地番と重なった図形のOID1件
	 */
	@SuppressWarnings("unchecked")
	public List<Oid> getBufferIntersectsOid(List<Integer> lotNumberIdList, Integer applicationId,
			String targetTableName, int epsg, double buffer) {
		LOGGER.debug("指定レイヤと指定地番図形(バッファ)が重なるか判定 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String getLotNumberQuery = "";
			if (applicationId == null) {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    ST_Transform(" + //
						"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
						"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
						"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
						"  FROM " + //
						"    f_lot_number " + //
						"  WHERE " + //
						"    chiban_id IN (:lotNumbers) ";
			} else {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    ST_Transform(" + //
						"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
						"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
						"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
						"  FROM " + //
						"    f_application_lot_number " + //
						"  WHERE " + //
						"    application_id = :applicationId ";
			}
			String sql = "" + //
					"WITH lon_number_geom AS ( " + // 地番を特定し、バッファを発生
					getLotNumberQuery + //
					") " + //
					"SELECT DISTINCT " + //
					"  a.ogc_fid AS oid " + //
					"FROM " + //
					"  " + targetTableName + " AS a " + //
					"INNER JOIN " + //
					"  lon_number_geom AS b " + // 接触確認
					"ON " + //
					"  ST_Intersects(a.wkb_geometry, b.geom) " + //
					"ORDER BY a.ogc_fid ASC";
			if (applicationId == null) {
				return em.createNativeQuery(sql, Oid.class).setParameter("lotNumbers", lotNumberIdList)
						.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
			} else {
				return em.createNativeQuery(sql, Oid.class).setParameter("applicationId", applicationId)
						.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
			}

		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定レイヤと指定地番図形(バッファ)が重なるか判定 終了");
		}
	}

	/**
	 * 指定レイヤと指定地番図形が重ならない場合の距離取得
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param applicationId   申請ID
	 * @param targetTableName 比較テーブル名
	 * @return 距離
	 */
	@SuppressWarnings("unchecked")
	public List<Distance> getDistance(List<Integer> lotNumberIdList, Integer applicationId, String targetTableName,
			int epsg) {
		LOGGER.debug("指定レイヤと指定地番図形が重ならない場合の距離取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String getLotNumberQuery = "";
			if (applicationId == null) {
				getLotNumberQuery = "" + //
						" SELECT " + //
						" ST_Union(geom) AS geom" + //
						" FROM f_lot_number" + //
						" WHERE chiban_id IN (:lotNumbers)";
			} else {
				getLotNumberQuery = "" + //
						" SELECT " + //
						" ST_Union(geom) AS geom" + //
						" FROM f_application_lot_number" + //
						" WHERE application_id = :applicationId";
			}
			String sql = "" + //
					" WITH lot_number_geom AS" + //
					" (" + //
					getLotNumberQuery + //
					")," + //
					" judgement_layer_geom AS" + //
					" (" + //
					" SELECT" + //
					" ST_Union(wkb_geometry) AS geom" + //
					" FROM " + //
					targetTableName + //
					" )" + //
					" SELECT" + //
					" ST_Distance(" + " CAST(ST_Transform(a.geom, :epsg) AS geography)," + //
					" CAST(ST_Transform(b.geom, :epsg) AS geography))" + " AS distance" + " FROM lot_number_geom AS a" + //
					" CROSS JOIN judgement_layer_geom AS b";

			if (applicationId == null) {
				return em.createNativeQuery(sql, Distance.class).setParameter("lotNumbers", lotNumberIdList)
						.setParameter("epsg", epsg).getResultList();
			} else {
				return em.createNativeQuery(sql, Distance.class).setParameter("applicationId", applicationId)
						.setParameter("epsg", epsg).getResultList();
			}

		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定レイヤと指定地番図形が重ならない場合の距離取得 終了");
		}
	}

	/**
	 * 指定テーブルのと指定カラム値取得
	 * 
	 * @param tableName  テーブル名
	 * @param columnName カラム名
	 * @param oid        OID
	 * @return カラム値
	 */
	@SuppressWarnings("unchecked")
	public List<ColumnValue> getColumnValue(String tableName, String columnName, int oid) {
		LOGGER.debug("指定テーブルの指定カラム値取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			String sql = "" + //
					"SELECT CAST(" + columnName + " AS TEXT) AS val " + //
					"FROM " + tableName + " " + //
					"WHERE ogc_fid = :oid";

			return em.createNativeQuery(sql, ColumnValue.class).setParameter("oid", oid).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("指定テーブルの指定カラム値取得 終了");
		}
	}

	/**
	 * 道路LOD2レイヤと指定地番図形バッファが重なるか判定
	 * 
	 * @param lotNumberIdList 地番IDリスト
	 * @param applicationId   申請ID
	 * @param epsg            データの座標系
	 * @param buffer          バッファ
	 * @return 重なった道路LOD2レイヤの属性情報
	 */
	@SuppressWarnings("unchecked")
	public List<RoadLod2> getIntersectsRoadLod2(List<Integer> lotNumberIdList, Integer applicationId, int epsg,
			double buffer) {
		LOGGER.debug("道路LOD2バッファ重なり判定 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String getLotNumberQuery = "";
			if (applicationId == null) {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    ST_Transform(" + //
						"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
						"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
						"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
						"  FROM " + //
						"    f_lot_number " + //
						"  WHERE " + //
						"    chiban_id IN (:lotNumbers) ";
			} else {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    ST_Transform(" + //
						"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
						"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
						"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
						"  FROM " + //
						"    f_application_lot_number " + //
						"  WHERE " + //
						"    application_id = :applicationId ";
			}
			String sql = "" + //
					"WITH lon_number_geom AS ( " + // 地番を特定し、バッファを発生
					getLotNumberQuery + //
					")   " + //
					"  SELECT DISTINCT   " + //
					"    a.object_id AS object_id, a.width AS width, a.line_number AS line_number ,a.function AS function"
					+ //
					"  FROM f_road_lod2 AS a   " + //
					"  INNER JOIN   " + //
					"    lon_number_geom AS b   " + // 接触確認
					"  ON   " + //
					"    ST_Intersects(a.geom, b.geom)   " + //
					"    WHERE (a.t_function = 1000  OR a.t_function = 1020) " + // 車道部と車道交差部(t_function = 1000 OR
																					// t_function = 1020)のみ対象とする
					"  ORDER BY a.object_id ASC";
			// LOGGER.debug(sql);
			if (applicationId == null) {
				return em.createNativeQuery(sql, RoadLod2.class).setParameter("lotNumbers", lotNumberIdList)
						.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
			} else {
				return em.createNativeQuery(sql, RoadLod2.class).setParameter("applicationId", applicationId)
						.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
			}
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("道路LOD2バッファ重なり判定 終了");
		}
	}

	/**
	 * 車道部に接する歩道を取得する
	 * 
	 * @param objectId オブジェクトID
	 * @return 隣接歩道の道路LOD2フィーチャ
	 */
	public List<RoadLod2> getWalkWaysIntersectsRoadWay(Integer objectId, String lineNumber) {
		LOGGER.debug("道路LOD2歩道有無判定 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			if (lineNumber != null) {
				// 路線番号指定がある場合、路線番号の同じ道路LOD2レイヤを使用して隣接歩道を取得する
				String sql = "" + //
						"WITH roadway_geom AS (" + //
						"  SELECT geom, crossid FROM f_road_lod2 WHERE line_number = :lineNumber AND (t_function = 1000  OR t_function = 1020) "
						+ //
						")" + //
						"SELECT DISTINCT    " + //
						"  a.object_id AS object_id, a.width AS width, a.line_number AS line_number, a.function AS function "
						+ //
						"FROM f_road_lod2 AS a  " + //
						"INNER JOIN    " + //
						"  roadway_geom AS b     " + //
						"ON    " + //
						"  ST_Intersects(a.geom, b.geom)  " + //
						" AND a.crossid = b.crossid " + //
						"WHERE a.t_function = 2000   " + //
						"ORDER BY a.object_id ASC;";
				return em.createNativeQuery(sql, RoadLod2.class).setParameter("lineNumber", lineNumber).getResultList();
			} else {
				String sql = "" + //
						"WITH roadway_geom AS (" + //
						"  SELECT geom, crossid FROM f_road_lod2 WHERE object_id = :objectId" + //
						")" + //
						"SELECT DISTINCT    " + //
						"  a.object_id AS object_id, a.width AS width, a.line_number AS line_number, a.function AS function "
						+ //
						"FROM f_road_lod2 AS a  " + //
						"INNER JOIN    " + //
						"  roadway_geom AS b     " + //
						"ON    " + //
						"  ST_Intersects(a.geom, b.geom)  " + //
						" AND a.crossid = b.crossid " + //
						"WHERE a.t_function = 2000   " + //
						"ORDER BY a.object_id ASC;";
				return em.createNativeQuery(sql, RoadLod2.class).setParameter("objectId", objectId).getResultList();
			}

		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("道路LOD2歩道有無判定 終了");
		}
	}

	/**
	 * 地番+バッファと道路LOD2レイヤに重なる区割り線を取得
	 * 
	 * @param lotNumberIdList 地番リスト
	 * @param applicationId   申請ID
	 * @param epsg            座標系
	 * @param buffer          バッファ
	 * @param objectId        オブジェクトID
	 * @param lineNumber      路線番号
	 * @return 重なった区割り線の属性情報
	 */
	@SuppressWarnings("unchecked")
	public List<SplitLine> getSplitLineFromLotNumberAndRoadLod2(List<Integer> lotNumberIdList, Integer applicationId,
			int epsg, double buffer, Integer objectId, String lineNumber) {
		LOGGER.debug("地番+バッファと道路LOD2レイヤに重なる区割り線取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			final String get_lod2_geom_query = (lineNumber == null) ?
			// 路線番号が指定されていない場合、指定したオブジェクトIDの道路LOD2フィーチャを使用して区割り線を取得
					"SELECT geom FROM f_road_lod2 WHERE object_id = :objectId" :
					// 路線番号が指定されている場合、同一路線番号（車道部）フィーチャをユニオンしたフィーチャを使用して区割り線を取得
					" SELECT ST_UNION(geom) AS geom FROM f_road_lod2 WHERE line_number = :lineNumber AND (t_function = 1000  OR t_function = 1020) ";
			String getLotNumberQuery = "";
			if (applicationId == null) {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    ST_Transform(" + //
						"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
						"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
						"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
						"  FROM " + //
						"    f_lot_number " + //
						"  WHERE " + //
						"    chiban_id IN (:lotNumbers) ";
			} else {
				getLotNumberQuery = "" + //
						"  SELECT " + //
						"    ST_Transform(" + //
						"      CAST(ST_Buffer(" + // 型変換を「::」で実施すると、フレームワークでエラーとなるので、cast関数を使用する。
						"        CAST(ST_Transform(geom, 4612) AS geography), :buffer" + // 距離指定でバッファを発生させる場合は、一度地理座標系に変換し、geographyに変換すると精度の高いバッファができる
						"      ) AS geometry), :epsg) AS geom " + // geographyは元のgeometryに戻し、さらに元の座標系に変換する
						"  FROM " + //
						"    f_application_lot_number " + //
						"  WHERE " + //
						"    application_id = :applicationId ";
			}

			String sql = "" + //
			// 地番+バッファ
					"WITH lon_number_geom AS (  " + //
					getLotNumberQuery + //
					")," + //
					// 道路LOD2レイヤ
					"lod2_geom AS (" + //
					" " + get_lod2_geom_query + "  " + //
					")" + //
					// 区割り線取得
					"SELECT " + //
					"    object_id, road_width, roadway_width " + //
					"FROM f_split_line AS a " + //
					"INNER JOIN   " + //
					"	lon_number_geom AS b  " + //
					"ON   " + //
					"    ST_Intersects(a.geom, b.geom)" + //
					"INNER JOIN " + //
					"    lod2_geom AS c " + //
					"ON  ST_Intersects(a.geom, c.geom)";
			if (lineNumber == null) {
				if (applicationId == null) {
					return em.createNativeQuery(sql, SplitLine.class).setParameter("lotNumbers", lotNumberIdList)
							.setParameter("epsg", epsg).setParameter("buffer", buffer)
							.setParameter("objectId", objectId).getResultList();
				} else {
					return em.createNativeQuery(sql, SplitLine.class).setParameter("applicationId", applicationId)
							.setParameter("epsg", epsg).setParameter("buffer", buffer)
							.setParameter("objectId", objectId).getResultList();
				}

			} else {
				if (applicationId == null) {
					return em.createNativeQuery(sql, SplitLine.class).setParameter("lotNumbers", lotNumberIdList)
							.setParameter("epsg", epsg).setParameter("buffer", buffer)
							.setParameter("lineNumber", lineNumber).getResultList();
				} else {
					return em.createNativeQuery(sql, SplitLine.class).setParameter("applicationId", applicationId)
							.setParameter("epsg", epsg).setParameter("buffer", buffer)
							.setParameter("lineNumber", lineNumber).getResultList();
				}

			}
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("地番+バッファと道路LOD2レイヤに重なる区割り線取得 終了");
		}
	}

	/**
	 * 最近接道路中心位置取得
	 * 
	 * @param lotNumberIdList 申請地番リスト
	 * @param applicationId   申請ID
	 * @param objectId        オブジェクトID
	 * @param lineNumber      路線番号
	 * @return List<RoadCenterLinePosition> 最近接道路中心位置
	 */
	@SuppressWarnings("unchecked")
	public List<RoadCenterLinePosition> getRoadCenterLinePosition(List<Integer> lotNumberIdList, Integer applicationId,
			Integer objectId, String lineNumber) {
		LOGGER.debug("最近接道路中心位置取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			final String get_lod2_geom_query = (lineNumber == null) ?
			// 路線番号が指定されていない場合、指定したオブジェクトIDの道路LOD2フィーチャを使用して区割り線を取得
					"SELECT geom FROM f_road_lod2 WHERE object_id = :objectId" :
					// 路線番号が指定されている場合、同一路線番号フィーチャ（車道部）をユニオンしたフィーチャを使用して区割り線を取得
					" SELECT ST_UNION(geom) AS geom FROM f_road_lod2 WHERE line_number = :lineNumber AND (t_function = 1000  OR t_function = 1020)";
			// 地番取得
			final String get_lot_number_query = (applicationId == null) ?
			// 申請地番NULL -> F_地番
					"    SELECT geom AS geom FROM f_lot_number WHERE chiban_id IN (:lotNumbers) " :
					// 申請地番!=NULL -> F_申請地番
					"    SELECT geom AS geom FROM f_application_lot_number WHERE application_id = :applicationId ";
			String sql = "" + //
			// 選択地番の集合
					"WITH lot_numbers AS ( " + //
					get_lot_number_query + //
					")," + //
					// 選択地番の重心
					"center_lot AS (" + //
					"    SELECT ST_Centroid(ST_Collect(lot_numbers.geom)) AS geom FROM lot_numbers" + //
					")," + //
					// 全道路中心線
					"road_center AS (" + //
					"    SELECT geom, object_id FROM f_road_center_line" + //
					"), " + //
					// 道路LOD2
					"road_lod2 AS (" + //
					" " + get_lod2_geom_query + " " + //
					"), " + //
					// 道路LOD2と重なる道路中心線
					"nearest_road AS (" + //
					"    SELECT " + //
					"        object_id, ST_Distance(center_lot.geom, road_center.geom)  AS distance, " + //
					"        ST_LineMerge(road_center.geom) AS geom " + //
					"    FROM " + //
					"        center_lot, road_center, road_lod2" + //
					"    WHERE " + //
					"        ST_Intersects(road_center.geom, road_lod2.geom) = true " + //
					"    ORDER BY distance ASC LIMIT 1" + //
					")," + //
					// 最近接道路中心線位置
					"nearest_point AS (" + //
					"    SELECT " + //
					"        ST_LineInterpolatePoint(nearest_road.geom, " + //
					"        ST_LineLocatePoint(nearest_road.geom, center_lot.geom)) AS geom  " + //
					"    FROM nearest_road, center_lot" + //
					")" + //
					"SELECT " + //
					"    ST_AsText(nearest_point.geom) AS wkt, nearest_road.object_id AS object_id " + //
					"FROM nearest_point, nearest_road";
			if (lineNumber == null) {
				if (applicationId == null) {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("lotNumbers", lotNumberIdList).setParameter("objectId", objectId)
							.getResultList();
				} else {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("applicationId", applicationId).setParameter("objectId", objectId)
							.getResultList();
				}

			} else {
				if (applicationId == null) {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("lotNumbers", lotNumberIdList).setParameter("lineNumber", lineNumber)
							.getResultList();
				} else {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("applicationId", applicationId).setParameter("lineNumber", lineNumber)
							.getResultList();
				}
			}

		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("最近接道路中心位置取得 終了");
		}
	}

	/**
	 * 地番バッファと重なる道路中心線の長さから最近接道路中心位置取得
	 * 
	 * @param lotNumberIdList 申請地番リスト
	 * @param applicationId   申請ID
	 * @param objectId        オブジェクトID
	 * @param epsg            座標系
	 * @param buffer          バッファ
	 * @param lineNumber      路線番号
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<RoadCenterLinePosition> getRoadCenterLinePositionWithBuffer(List<Integer> lotNumberIdList,
			Integer applicationId, Integer objectId, int epsg, double buffer, String lineNumber) {
		LOGGER.debug("バッファ重複長から最近接道路中心位置取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			final String get_lod2_geom_query = (lineNumber == null) ?
			// 路線番号が指定されていない場合、指定したオブジェクトIDの道路LOD2フィーチャを使用して区割り線を取得
					"SELECT geom FROM f_road_lod2 WHERE object_id = :objectId" :
					// 路線番号が指定されている場合、同一路線番号フィーチャ（車道部）をユニオンしたフィーチャを使用して区割り線を取得
					" SELECT ST_UNION(geom) AS geom FROM f_road_lod2 WHERE line_number = :lineNumber AND (t_function = 1000  OR t_function = 1020)";
			// 地番取得
			final String get_lot_number_query = (applicationId == null) ?
			// 申請地番NULL -> F_地番
					"    SELECT geom AS geom FROM f_lot_number WHERE chiban_id IN (:lotNumbers) " :
					// 申請地番!=NULL -> F_申請地番
					"    SELECT geom AS geom FROM f_application_lot_number WHERE application_id = :applicationId ";
			String sql = "" + //
			// 選択地番の集合
					"WITH lot_numbers AS ( " + //
					get_lot_number_query + //
					")," + //
					// 選択地番の重心
					"center_lot AS (" + //
					"    SELECT ST_Centroid(ST_Collect(lot_numbers.geom)) AS geom FROM lot_numbers" + //
					")," + //
					// 全道路中心線
					"road_center AS (" + //
					"    SELECT geom, object_id FROM f_road_center_line" + //
					"), " + //
					// 道路LOD2
					"road_lod2 AS (" + //
					" " + get_lod2_geom_query + " " + //
					"), " + //
					// 地番バッファ
					"buffer_lot AS (" + //
					"  SELECT ST_Transform(  " + //
					"     CAST(ST_Buffer(  " + //
					"       CAST(ST_Transform(lot_numbers.geom, 4612) AS geography), :buffer  " + //
					"       ) AS geometry), :epsg) AS geom FROM lot_numbers " + //
					")," + //
					// 道路LOD2と重なる道路中心線
					"intersect_roadcenter_line AS (" + //
					"   SELECT " + //
					"     " + //
					"	road_center.object_id AS object_id, " + //
					"    ST_LineMerge(road_center.geom) AS geom, " + //
					// " ST_Length(ST_Intersection(road_center.geom, buffer_lot.geom)) AS length" +
					// //
					"    ST_Length(ST_Intersection(ST_Intersection(road_center.geom, road_lod2.geom), buffer_lot.geom)) AS length"
					+ //
					"   FROM road_center, road_lod2, buffer_lot " + //
					"   WHERE " + //
					"   ST_Intersects(road_center.geom, road_lod2.geom) = true " + //
					"   AND " + //
					"   ST_Intersects(road_center.geom, buffer_lot.geom) = true" + //
					"   ORDER BY length DESC LIMIT 1" + //
					"), " + //
					// 最近接道路中心線位置
					"nearest_point AS (" + //
					"    SELECT " + //
					"        ST_LineInterpolatePoint(intersect_roadcenter_line.geom, " + //
					"        ST_LineLocatePoint(intersect_roadcenter_line.geom, center_lot.geom)) AS geom  " + //
					"    FROM intersect_roadcenter_line, center_lot" + //
					")" + //
					"SELECT " + //
					"    ST_AsText(nearest_point.geom) AS wkt, intersect_roadcenter_line.object_id AS object_id " + //
					"FROM nearest_point, intersect_roadcenter_line";
			if (lineNumber == null) {
				if (applicationId == null) {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("lotNumbers", lotNumberIdList).setParameter("objectId", objectId)
							.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
				} else {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("applicationId", applicationId).setParameter("objectId", objectId)
							.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
				}
				
			} else {
				if (applicationId == null) {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("lotNumbers", lotNumberIdList).setParameter("lineNumber", lineNumber)
							.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
				} else {
					return em.createNativeQuery(sql, RoadCenterLinePosition.class)
							.setParameter("applicationId", applicationId).setParameter("lineNumber", lineNumber)
							.setParameter("epsg", epsg).setParameter("buffer", buffer).getResultList();
				}
				
			}
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("バッファ重複長から最近接道路中心位置取得 終了");
		}
	}

	/**
	 * 道路中心線を最近接道路中心位置で分割したジオメトリを取得する
	 * 
	 * @param wkt      WKT
	 * @param objectId オブジェクトID
	 * @param buffer   バッファ
	 * @return 分割道路中心線
	 */
	@SuppressWarnings("unchecked")
	public List<SplitRoadCenterLine> getSplitRoadCenterLine(String wkt, int epsg, Integer objectId, Double buffer) {
		LOGGER.debug("分割道路中心線取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
			// 最近接道路中心線
					"WITH nearest_road AS (" + //
					"    SELECT ST_LineMerge(geom) AS geom FROM f_road_center_line WHERE object_id = :objectId" + //
					"), " + //
					// 分割ジオメトリ
					"split_dump AS ( " + //
					"    SELECT ST_Dump(" + //
					"        ST_Difference(" + //
					"            nearest_road.geom, " + //
					"            ST_Buffer(ST_GeomFromText(:wkt, :epsg), :buffer)" + //
					"        )" + //
					"        ) AS dmp FROM nearest_road" + //
					")" + //
					"SELECT " + //
					"    ROW_NUMBER() OVER(ORDER BY dmp ASC) AS id, " + //
					"    ST_AsText((dmp).geom) AS wkt " + //
					"FROM split_dump";
			return em.createNativeQuery(sql, SplitRoadCenterLine.class).setParameter("objectId", objectId)
					.setParameter("epsg", epsg).setParameter("wkt", wkt).setParameter("buffer", buffer).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("分割道路中心線取得 終了");
		}

	}

	/**
	 * 分割道路中心線に重なる区割り線を最近接道路中心位置からの距離順で取得
	 * 
	 * @param roadCenterLineWkt     道路中心線のWKT
	 * @param roadCenterPositionWkt 最近接道路中心位置のWKT
	 * @param epsg                  座標系
	 * @return 区割り線
	 */
	@SuppressWarnings("unchecked")
	public List<SplitLine> getSplitLineWithSplitRoadCenterLine(String roadCenterLineWkt, String roadCenterPositionWkt,
			int epsg) {
		LOGGER.debug("分割道路中心線に重なる区割り線取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"    object_id, road_width, roadway_width " + //
					"FROM f_split_line AS a " + //
					"WHERE ST_Intersects(a.geom, " + //
					"    ST_GeomFromText(:linewkt, :epsg)) = True" + //
					"    ORDER BY  ST_Distance(ST_GeomFromText(:pointwkt, :epsg), ST_LineMerge(a.geom)) ASC";
			return em.createNativeQuery(sql, SplitLine.class).setParameter("linewkt", roadCenterLineWkt)
					.setParameter("pointwkt", roadCenterPositionWkt).setParameter("epsg", epsg).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("分割道路中心線に重なる区割り線取得 終了");
		}
	}

	/**
	 * 路線番号が同じ道路LOD2フィーチャを取得する
	 * 
	 * @param lineNumber 路線番号
	 * @return List<RoadLod2> 道路LOD2フィーチャ
	 */
	public List<RoadLod2> getRoadLod2WithLineNumber(String lineNumber) {
		LOGGER.debug("路線番号が同じ道路LOD2フィーチャを取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"object_id, width, line_number, function " + //
					"FROM f_road_lod2 " + //
					"WHERE line_number = :lineNumber AND (t_function = 1000  OR t_function = 1020)";
			return em.createNativeQuery(sql, RoadLod2.class).setParameter("lineNumber", lineNumber).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("路線番号が同じ道路LOD2フィーチャを取得 終了");
		}
	}

	/**
	 * 区割り線のエクステント取得
	 * 
	 * @param epsg 座標系
	 * @return List<SpiltLineExtent> 区割り線エクステント
	 */
	public List<SpiltLineExtent> getSplitLineExtent(int epsg, List<SplitLine> splitLine) {
		LOGGER.debug("区割り線のエクステント取得 開始");
		EntityManager em = null;
		try {
			final List<Integer> oidList = new ArrayList<Integer>();
			for (SplitLine aLine : splitLine) {
				oidList.add(aLine.getObjectId());
			}
			em = emf.createEntityManager();
			String sql = "" + //
					"WITH line_merged AS ( " + //
					"  SELECT " + //
					"  ST_XMin(ST_Transform(geom, " + epsg + ")) AS minlon, " + //
					"  ST_XMax(ST_Transform(geom, " + epsg + ")) AS maxlon, " + //
					"  ST_YMin(ST_Transform(geom, " + epsg + ")) AS minlat, " + //
					"  ST_YMax(ST_Transform(geom, " + epsg + ")) AS maxlat " + //
					"  FROM f_split_line " + //
					"  WHERE object_id IN (:splitLine) " + //
					") " + //
					"SELECT " + //
					"ROW_NUMBER() OVER() AS id, " + //
					"MIN(minlon) AS minlon, " + //
					"MAX(maxlon) AS maxlon, " + //
					"MIN(minlat) AS minlat, " + //
					"MAX(maxlat) AS maxlat " + //
					"FROM line_merged";
			return em.createNativeQuery(sql, SpiltLineExtent.class).setParameter("splitLine", oidList).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("区割り線のエクステント取得 終了");
		}
	}

	/**
	 * 道路LOD2地物のエクステントを取得する
	 * 
	 * @param epsg     EPSG
	 * @param objectId オブジェクトID
	 * @return List<SpiltLineExtent> 道路LOD2エクステント
	 */
	public List<SpiltLineExtent> getRoadLOD2Extent(int epsg, int objectId) {
		LOGGER.debug("道路LOD2のエクステント取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"SELECT " + //
					"  ROW_NUMBER() OVER() AS id, " + //
					"  ST_XMin(ST_Transform(geom, " + epsg + ")) AS minlon,  " + //
					"  ST_XMax(ST_Transform(geom,  " + epsg + ")) AS maxlon,  " + //
					"  ST_YMin(ST_Transform(geom,  " + epsg + ")) AS minlat,  " + //
					"  ST_YMax(ST_Transform(geom,  " + epsg + ")) AS maxlat " + //
					"FROM f_road_lod2 " + //
					"WHERE object_id = :objectId";
			return em.createNativeQuery(sql, SpiltLineExtent.class).setParameter("objectId", objectId).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("道路LOD2のエクステント取得 終了");
		}
	}

	/**
	 * 道路LOD2地物（複数オブジェクトIDを指定）のエクステントを取得する
	 * 
	 * @param epsg    EPSG
	 * @param oidList オブジェクトIDのリスト
	 * @return List<SpiltLineExtent> 道路LOD2エクステント
	 */
	public List<SpiltLineExtent> getRoadLOD2Extent(int epsg, List<Integer> oidList) {
		LOGGER.debug("道路LOD2のエクステント取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql = "" + //
					"WITH line_merged AS ( " + //
					"  SELECT " + //
					"  ST_XMin(ST_Transform(geom, " + epsg + ")) AS minlon, " + //
					"  ST_XMax(ST_Transform(geom, " + epsg + ")) AS maxlon, " + //
					"  ST_YMin(ST_Transform(geom, " + epsg + ")) AS minlat, " + //
					"  ST_YMax(ST_Transform(geom, " + epsg + ")) AS maxlat " + //
					"  FROM f_road_lod2 " + //
					"  WHERE object_id IN (:lod2) " + //
					") " + //
					"SELECT " + //
					"ROW_NUMBER() OVER() AS id, " + //
					"MIN(minlon) AS minlon, " + //
					"MAX(maxlon) AS maxlon, " + //
					"MIN(minlat) AS minlat, " + //
					"MAX(maxlat) AS maxlat " + //
					"FROM line_merged";
			return em.createNativeQuery(sql, SpiltLineExtent.class).setParameter("lod2", oidList).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("道路LOD2のエクステント取得 終了");
		}
	}
}
