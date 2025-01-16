package developmentpermission.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import developmentpermission.entity.ApplicationLotNumber;
import developmentpermission.entity.ApplyLotNumber;
import developmentpermission.entity.LotNumber;
import developmentpermission.entity.LotNumberAndDistrict;
import developmentpermission.form.LotNumberForm;

/**
 * F_地番DAO
 */
@Transactional
public class LotNumberDao extends AbstractDao {

	/** LOGGER */
	private static final Logger LOGGER = LoggerFactory.getLogger(LotNumberDao.class);

	/**
	 * コンストラクタ
	 * 
	 * @param emf Entityマネージャファクトリ
	 */
	public LotNumberDao(EntityManagerFactory emf) {
		super(emf);
	}

	/**
	 * 地番検索
	 * 
	 * @param districtId  地番ID(完全一致検索)
	 * @param chiban      地番(部分一致検索)
	 * @param lonlatEpsg  緯度経度座標系EPSG
	 * @param isGoverment 行政かどうか
	 * @return 検索結果
	 */
	@SuppressWarnings("unchecked")
	public List<LotNumberAndDistrict> searchLotNumberList(String districtId, String chiban, int lonlatEpsg,
			boolean isGoverment) {
		LOGGER.debug("地番検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			// WHERE句構築
			StringBuffer where = new StringBuffer();
			if (districtId != null) {
				// 地番ID指定あり
				appendWhereText(where);
				where.append("a.district_id = :districtId ");
			}
			if (chiban != null && !"".equals(chiban)) {
				// 地番文字列指定あり
				appendWhereText(where);
				where.append("COALESCE(a.chiban, '') LIKE CONCAT(:chiban, '%') ");
			}

			String sql;
			if (isGoverment) {
				// 行政向け
				sql = "" + //
						"SELECT " + //
						"  a.chiban_id AS chiban_id, " + //
						"  a.district_id AS district_id, " + //
						"  a.chiban AS chiban, " + //
						"  a.result_column1 AS result_column1, " + //
						"  a.result_column2 AS result_column2, " + //
						"  a.result_column3 AS result_column3, " + //
						"  a.result_column4 AS result_column4, " + //
						"  a.result_column5 AS result_column5, " + //
						"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lon, " + //
						"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lat, " + //
						"  ST_XMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlon, " + //
						"  ST_YMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlat, " + //
						"  ST_XMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlon, " + //
						"  ST_YMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlat, " + //
						"  b.district_name AS ooaza_district_name, " + //
						"  b.district_kana AS ooaza_district_kana, " + //
						"  b.result_column1 AS ooaza_result_column1, " + //
						"  b.result_column2 AS ooaza_result_column2, " + //
						"  b.result_column3 AS ooaza_result_column3, " + //
						"  b.result_column4 AS ooaza_result_column4, " + //
						"  b.result_column5 AS ooaza_result_column5, " + //
						"  e.status AS status, " + //
						"  e.application_id AS application_id, " + //
						"  e.full_flag AS full_flag " + //
						"FROM " + //
						"  f_lot_number AS a " + //
						"LEFT OUTER JOIN " + //
						"  f_district AS b " + //
						"ON " + //
						"  a.district_id = b.district_id " + //
						"LEFT JOIN LATERAL ( " + //
						"  SELECT " + //
						"    d.application_id, " + //
						"    d.status, " + //
						"    c.full_flag " + //
						"  FROM  " + //
						"    o_application_lot_number AS c " + //
						"  LEFT OUTER JOIN " + //
						"    o_application AS d " + //
						"  ON " + //
						"    c.application_id = d.application_id " + //
						"  WHERE " + //
						"    a.chiban_id = c.lot_number_id " + //
						"  ORDER BY " + //
						"    d.update_datetime DESC NULLS LAST " + //
						"  LIMIT 1 " + // update_datetimeで降順ソートして最新の1件のみを取得
						") AS e ON true " + //
						where + //
						"ORDER BY " + //
						"  b.disp_order ASC, " + //
						"  a.chiban ASC " + "LIMIT 5001";
			} else {
				// 事業者向け
				sql = "" + //
						"SELECT " + //
						"  a.chiban_id AS chiban_id, " + //
						"  a.district_id AS district_id, " + //
						"  a.chiban AS chiban, " + //
						"  a.result_column1 AS result_column1, " + //
						"  a.result_column2 AS result_column2, " + //
						"  a.result_column3 AS result_column3, " + //
						"  a.result_column4 AS result_column4, " + //
						"  a.result_column5 AS result_column5, " + //
						"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lon, " + //
						"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lat, " + //
						"  ST_XMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlon, " + //
						"  ST_YMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlat, " + //
						"  ST_XMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlon, " + //
						"  ST_YMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlat, " + //
						"  b.district_name AS ooaza_district_name, " + //
						"  b.district_kana AS ooaza_district_kana, " + //
						"  b.result_column1 AS ooaza_result_column1, " + //
						"  b.result_column2 AS ooaza_result_column2, " + //
						"  b.result_column3 AS ooaza_result_column3, " + //
						"  b.result_column4 AS ooaza_result_column4, " + //
						"  b.result_column5 AS ooaza_result_column5, " + //
						"  null AS status, " + // null固定
						"  null AS application_id, " + // null固定
						"  '0' AS full_flag " + //
						"FROM " + //
						"  f_lot_number AS a " + //
						"LEFT OUTER JOIN " + //
						"  f_district AS b " + //
						"ON " + //
						"  a.district_id = b.district_id " + //
						where + //
						"ORDER BY " + //
						"  b.disp_order ASC, " + //
						"  a.chiban ASC " + "LIMIT 5001";
			}

			Query query = em.createNativeQuery(sql, LotNumberAndDistrict.class);
			if (districtId != null) {
				// 地番ID指定あり
				query = query.setParameter("districtId", districtId);
			}
			if (chiban != null && !"".equals(chiban)) {
				// 地番文字列指定あり
				query = query.setParameter("chiban", chiban);
			}
			return query.getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("地番検索 終了");
		}
	}

	/**
	 * 地番検索(行政)
	 * 
	 * @param longitude   経度
	 * @param latitude    緯度
	 * @param epsg        システムEPSG
	 * @param lonlatEpsg  緯度経度座標系EPSG
	 * @param isGoverment 行政かどうか
	 * @return 検索結果
	 */
	@SuppressWarnings("unchecked")
	public List<ApplyLotNumber> searchApplyingLotNumberList(String longitude, String latitude, int epsg,
			int lonlatEpsg) {
		LOGGER.debug("座標地番検索（行政） 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql;
			// 事業者向け
			sql = "" + //
					"SELECT " + //
					"  a.application_id AS application_id, " + //
					"  a.lot_numbers AS lot_numbers, " + //
					"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lon, " + //
					"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lat, " + //
					"  ST_XMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlon, " + //
					"  ST_YMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlat, " + //
					"  ST_XMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlon, " + //
					"  ST_YMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlat, " + //
					"  b.status AS status " + //
					"FROM " + //
					"  f_application_lot_number AS a " + //
					"LEFT OUTER JOIN " + //
					"    o_application AS b " + //
					"ON " + //
					"  b.application_id = a.application_id " + //
					"WHERE " + //
					"  ST_Intersects(a.geom, ST_Transform(ST_SetSRID(ST_Point(:longitude, :latitude), " + lonlatEpsg
					+ "), " + epsg + ")) " + //
					"ORDER BY " + //
					"  a.application_id ASC";
			Query query = em.createNativeQuery(sql, ApplyLotNumber.class);
			query = query.setParameter("longitude", Double.parseDouble(longitude));
			query = query.setParameter("latitude", Double.parseDouble(latitude));
			List<ApplyLotNumber> resultList = query.getResultList();
			// 地番に紐づく申請を取得し、結果に追加
			// 単純なLEFT JOIN だと結果が上書きされる?ため再度取得する形で実装
			List<ApplyLotNumber> resList = new ArrayList<ApplyLotNumber>();
			for (ApplyLotNumber aResult : resultList) {
				em.clear();
				final String sql2 = "SELECT ROW_NUMBER() OVER(ORDER BY a.application_id ASC) seq_id, " + //
						"null AS application_id, " + //
						"null AS lot_number_id, " + //
						"'0' AS full_flag, " + //
						"a.status AS status " + //
						"FROM o_application AS a " + //
						"WHERE application_id = :application_id " + //
						"ORDER BY application_id ASC";
				Query query2 = em.createNativeQuery(sql2, ApplicationLotNumber.class);
				query2.setParameter("application_id", aResult.getApplicationId());
				List<ApplicationLotNumber> applicationList = query2.getResultList();
				if (applicationList.size() == 0) {
					resList.add(aResult);
				} else {
					for (ApplicationLotNumber aApp : applicationList) {
						ApplyLotNumber newResult = new ApplyLotNumber();
						newResult.setApplicationId(aResult.getApplicationId());
						newResult.setLotNumbers(aResult.getLotNumbers());
						newResult.setLon(aResult.getLon());
						newResult.setLat(aResult.getLat());
						newResult.setMinlon(aResult.getMinlon());
						newResult.setMinlat(aResult.getMinlat());
						newResult.setMaxlon(aResult.getMaxlon());
						newResult.setMaxlat(aResult.getMaxlat());
						// 申請ID/ステータスは個別に取得した値を格納
						newResult.setStatus(aApp.getStatus());
						resList.add(newResult);
					}
				}
			}
			return resList;
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("座標地番検索（行政） 終了");
		}
	}

	/**
	 * 地番検索(事業者)
	 * 
	 * @param longitude   経度
	 * @param latitude    緯度
	 * @param epsg        システムEPSG
	 * @param lonlatEpsg  緯度経度座標系EPSG
	 * @param isGoverment 行政かどうか
	 * @return 検索結果
	 */
	@SuppressWarnings("unchecked")
	public List<LotNumberAndDistrict> searchLotNumberList(String longitude, String latitude, int epsg, int lonlatEpsg) {
		LOGGER.debug("座標地番検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			String sql;
			// 事業者向け
			sql = "" + //
					"SELECT " + //
					"  a.chiban_id AS chiban_id, " + //
					"  a.district_id AS district_id, " + //
					"  a.chiban AS chiban, " + //
					"  a.result_column1 AS result_column1, " + //
					"  a.result_column2 AS result_column2, " + //
					"  a.result_column3 AS result_column3, " + //
					"  a.result_column4 AS result_column4, " + //
					"  a.result_column5 AS result_column5, " + //
					"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lon, " + //
					"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lat, " + //
					"  ST_XMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlon, " + //
					"  ST_YMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlat, " + //
					"  ST_XMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlon, " + //
					"  ST_YMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlat, " + //
					"  b.district_name AS ooaza_district_name, " + //
					"  b.district_kana AS ooaza_district_kana, " + //
					"  b.result_column1 AS ooaza_result_column1, " + //
					"  b.result_column2 AS ooaza_result_column2, " + //
					"  b.result_column3 AS ooaza_result_column3, " + //
					"  b.result_column4 AS ooaza_result_column4, " + //
					"  b.result_column5 AS ooaza_result_column5, " + //
					"  null AS status, " + // null固定
					"  null AS application_id, " + // null固定
					"  '0' AS full_flag " + // 0固定
					"FROM " + //
					"  f_lot_number AS a " + //
					"LEFT OUTER JOIN " + //
					"  f_district AS b " + //
					"ON " + //
					"  a.district_id = b.district_id " + //
					"WHERE " + //
					"  ST_Intersects(a.geom, ST_Transform(ST_SetSRID(ST_Point(:longitude, :latitude), " + lonlatEpsg
					+ "), " + epsg + ")) " + //
					"ORDER BY " + //
					"  b.disp_order ASC, " + //
					"  a.chiban_id ASC";

			Query query = em.createNativeQuery(sql, LotNumberAndDistrict.class);
			query = query.setParameter("longitude", Double.parseDouble(longitude));
			query = query.setParameter("latitude", Double.parseDouble(latitude));
			List<LotNumberAndDistrict> resultList = query.getResultList();
			// 事業者の場合結果をそのまま返却
			return resultList;
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("座標地番検索 終了");
		}
	}

	/**
	 * 地番図形検索（事業者）
	 * 
	 * @param wkt        図形のWKT
	 * @param epsg       データ座標系
	 * @param lonlatEpsg 表示座標系
	 * @param limit      取得上限
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<LotNumberAndDistrict> searchLotNumberLByFigure(String wkt, int epsg, int lonlatEpsg, int limit) {
		LOGGER.debug("図形地番検索 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();

			// 事業者向け
			String sql = "" + //
					"SELECT " + //
					"  a.chiban_id AS chiban_id, " + //
					"  a.district_id AS district_id, " + //
					"  a.chiban AS chiban, " + //
					"  a.result_column1 AS result_column1, " + //
					"  a.result_column2 AS result_column2, " + //
					"  a.result_column3 AS result_column3, " + //
					"  a.result_column4 AS result_column4, " + //
					"  a.result_column5 AS result_column5, " + //
					"  ST_X(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lon, " + //
					"  ST_Y(ST_Centroid(ST_Envelope(ST_Transform(a.geom, " + lonlatEpsg + ")))) AS lat, " + //
					"  ST_XMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlon, " + //
					"  ST_YMin(ST_Transform(a.geom, " + lonlatEpsg + ")) AS minlat, " + //
					"  ST_XMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlon, " + //
					"  ST_YMax(ST_Transform(a.geom, " + lonlatEpsg + ")) AS maxlat, " + //
					"  b.district_name AS ooaza_district_name, " + //
					"  b.district_kana AS ooaza_district_kana, " + //
					"  b.result_column1 AS ooaza_result_column1, " + //
					"  b.result_column2 AS ooaza_result_column2, " + //
					"  b.result_column3 AS ooaza_result_column3, " + //
					"  b.result_column4 AS ooaza_result_column4, " + //
					"  b.result_column5 AS ooaza_result_column5, " + //
					"  null AS status, " + // null固定
					"  null AS application_id, " + // null固定
					"  '0' AS full_flag " + // 0固定
					"FROM " + //
					"  f_lot_number AS a " + //
					"LEFT OUTER JOIN " + //
					"  f_district AS b " + //
					"ON " + //
					"  a.district_id = b.district_id " + //
					"WHERE " + //
					// 「重なる」から「内包する」に変更
					// " ST_Intersects(a.geom, ST_Transform(ST_GeomFromText(:wkt, " + lonlatEpsg
					// + "), " + epsg + ")) " + //
					"  ST_Contains(ST_Transform(ST_GeomFromText(:wkt, " + lonlatEpsg + //
					"), " + epsg + "), a.geom) " + //
					"ORDER BY " + //
					"  b.disp_order ASC, " + //
					"  a.chiban_id ASC";
			Query query = em.createNativeQuery(sql, LotNumberAndDistrict.class);
			query.setParameter("wkt", wkt);
			query.setMaxResults(limit);
			List<LotNumberAndDistrict> resultList = query.getResultList();
			return resultList;
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("図形地番検索 終了");
		}
	}

	/**
	 * ディゾルブした地番図形を取得する
	 * 
	 * @param lotNumbers 地番
	 * @return ディゾルブされた地番のwkt形式文字列
	 */
	@SuppressWarnings("unchecked")
	public String getDissolvedLotNumberWkt(List<Integer> lotNumbers) {
		LOGGER.debug("地番ディゾルブ図形取得 開始");
		EntityManager em = null;
		String res = null;
		try {
			em = emf.createEntityManager();
			String sql = //
					"SELECT ST_AsText(ST_Union(geom)) AS wkt " + //
							"FROM f_lot_number WHERE chiban_id in (:lotNumbers)";
			Object result = em.createNativeQuery(sql).setParameter("lotNumbers", lotNumbers).getSingleResult();
			res = result.toString();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("地番ディゾルブ図形取得 終了");
		}
		return res;
	}

	/**
	 * F_申請地番からF_地番を取得する
	 * 
	 * @param applicationId 申請ID
	 * @param epsg          座標系
	 * @return 地番一覧
	 */
	@SuppressWarnings("unchecked")
	public List<LotNumber> getLotNumberMasterFromApplicationId(int applicationId, int epsg) {
		LOGGER.debug("F_申請地番からF_地番取得 開始");
		EntityManager em = null;
		try {
			em = emf.createEntityManager();
			final String sql = "" + //
					"WITH application_geom AS ( " + //
					" SELECT geom AS geom " + //
					" FROM f_application_lot_number " + //
					" WHERE application_id = :applicationId " + //
					"), " + //
					"buffer_geom AS ( " + // 申請地番に微小なバッファを生じさせ内包する地番を取得
					"  SELECT ST_Transform( " + //
					"    CAST(ST_Buffer( " + //
					"       CAST(ST_Transform(application_geom.geom, 4612) AS geography), 0.5 " + //
					"    ) AS geometry), :epsg) AS geom " + //
					"  FROM application_geom " + //
					") " + //
					"SELECT  " + //
					"a.chiban_id AS chiban_id, " + //
					"a.district_id AS district_id, " + //
					"a.chiban AS chiban, " + //
					"a.result_column1 AS result_column1, " + //
					"a.result_column2 AS result_column2, " + //
					"a.result_column3 AS result_column3, " + //
					"a.result_column4 AS result_column4, " + //
					"a.result_column5 AS result_column5 " + //
					"FROM f_lot_number AS a, buffer_geom AS b " + //
					"WHERE ST_Contains(b.geom, a.geom) = true";
			return em.createNativeQuery(sql, LotNumber.class).setParameter("applicationId", applicationId).setParameter("epsg", epsg).getResultList();
		} finally {
			if (em != null) {
				em.close();
			}
			LOGGER.debug("F_申請地番からF_地番取得 終了");
		}

	}
}
