package com.kpmg.datalake.web.service;

import com.kpmg.datalake.common.constants.Constants;
import com.kpmg.datalake.common.database.DatabaseTemplate;
import com.kpmg.datalake.common.database.DatabaseUtils;
import com.kpmg.datalake.common.utils.StatementUtils;
import com.kpmg.datalake.common.utils.Utils;
import com.kpmg.datalake.common.vo.ServerResponse;
import com.kpmg.datalake.db.dao.*;
import com.kpmg.datalake.db.model.KpmgChkactSrcdtl;
import com.kpmg.datalake.db.model.KpmgSbjTpCrpnd;
import com.kpmg.datalake.db.model.Project;
import com.kpmg.datalake.db.model.SysUsr;
import com.kpmg.datalake.service.AccountCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * @author Echo
 * @create 2018-05-18
 * @desc 内部对账相关Service
 */
@Service
public class AccountCheckServiceImpl implements AccountCheckService {

    private String projectId = "56945C05E8FF43D2BE045EF6CC82285D";

    private static final Logger logger= LoggerFactory.getLogger(AccountCheckServiceImpl.class);

    /**
     * 原始明细
     */
    @Autowired
    private KpmgChkactSrcdtlMapper detailMapper;

    /**
     * 本方记录
     */
    @Autowired
    private KpmgChkactSelfprtMapper hostMapper;

    /**
     * 交易对手记录
     */
    @Autowired
    private KpmgChkactCntrprtMapper tradeMapper;

    /**
     * 匹配批次
     */
    @Autowired
    private KpmgChkactBtchMapper batchMapper;

    /**
     * 科目类型
     */
    @Autowired
    private KpmgSbjTpCrpndMapper courseMapper;

    /**
     * 获取需要对账的内部公司集合
     *
     * @return
     */
    @Override
    public List<String> getCompanyList(SysUsr user) {
        List<String> compareObjList = new ArrayList<>();
        if (user != null) {
            Project project = Utils.getProject(projectId, user);
            Connection connection = DatabaseUtils.getConnection(project);
            StringBuilder querySql = new StringBuilder("SELECT CORP_NM FROM KPMG_CHKACT_SRCDTL GROUP BY CORP_NM");
            // 查询出集团内部公司列表
            List<Map<String, Object>> resultList = null;
            resultList = DatabaseTemplate.queryForList(querySql.toString(), connection);

            List<String> compayList = new ArrayList<>();
            for (Map<String, Object> item : resultList) {
                compayList.add(item.get("CORP_NM").toString());
            }
            //List<String> compayList = detailMapper.queryCompanyList();
            // String [] str = { "1", "2", "3", "4", "5"};
            //        1 - 2
            //        1 - 3
            //        1 - 4
            //
            //        2 - 3
            //        2 - 4

            //        3 - 4
            // 当前位置的，同它后面的位置的数匹配

            for (int i = 0; i < compayList.size(); i++) {
                for (int j = i + 1; j < compayList.size(); j++) {
                    if (j < compayList.size()) {
                        compareObjList.add(compayList.get(i) + "-" + compayList.get(j));
                    }
                }
            }
        }

        return compareObjList;
    }

    /**
     * 对账Handler
     */
    @Override
    public ServerResponse compareHandler(SysUsr user) {
        Project project = Utils.getProject(projectId, user);
        Connection connection = DatabaseUtils.getConnection(project);
        // this.oneToOneCheck(user, connection);
        Map<String, List<KpmgChkactSrcdtl>> hostList = this.getObjList(user, "2017", "01", "01");
        Map<String, List<KpmgChkactSrcdtl>> tradeList = this.getObjList(user, "2017", "01", "02");
        Iterator<String> iterator = hostList.keySet().iterator();
        // 单边记账
        List<KpmgChkactSrcdtl> oneSideList = new ArrayList<>();

        List<Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>>> diffList = new ArrayList<>();
        // 遍历资产方map
        while (iterator.hasNext()) {
            String name = iterator.next();
            List<KpmgChkactSrcdtl> kpmgChkactSrcdtlList = hostList.get(name);
            // 遍历map中的明细集合
            Iterator<KpmgChkactSrcdtl> iterator1 = kpmgChkactSrcdtlList.iterator();
            while (iterator1.hasNext()) {
                KpmgChkactSrcdtl detailObj = iterator1.next();
                // 得到交易对手名字
                String tradeName = detailObj.getCstNm();
                BigDecimal amtEop = detailObj.getAmtEop();
                // 在负债方map中get
                if (tradeList.get(tradeName) != null) {
                    List<KpmgChkactSrcdtl> tradekpmgChkactSrcdtlList = tradeList.get(tradeName);
                    // 遍历负债中的明细集合
                    Iterator<KpmgChkactSrcdtl> iterator2 = tradekpmgChkactSrcdtlList.iterator();
                    List<KpmgChkactSrcdtl> tradeObjList = new ArrayList<>();
                    while (iterator2.hasNext()) {
                        KpmgChkactSrcdtl tradeDetail = iterator2.next();
                        if (tradeDetail.getCstNm().equals(name)) {
                            tradeObjList.add(tradeDetail);
                            iterator2.remove();
                        }
                    }
                    if (tradeObjList.size() == 0) {
                        oneSideList.add(detailObj);
                        iterator1.remove();
                        continue;
                    }
                    if (tradeObjList.size() == 1) {
                        Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> map = new HashMap<>();
                        detailObj.setDiffEop(detailObj.getAmtEop().subtract(tradeObjList.get(0).getAmtEop()));

                        map.put(detailObj, tradeObjList);
                        diffList.add(map);
                        iterator1.remove();
                        continue;
                    }
                    if (tradeObjList.size() > 1) {
                        BigDecimal temp = BigDecimal.ZERO;
                        for (KpmgChkactSrcdtl item : tradeObjList) {
                            temp = temp.add(item.getAmtEop());
                        }
                        if (amtEop.compareTo(temp) == 0) {
                            // 和相等抵消
                            iterator1.remove();
                            Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> map = new HashMap<>();
                            detailObj.setDiffEop(BigDecimal.ZERO);
                            map.put(detailObj, tradeObjList);
                            diffList.add(map);
                            continue;
                        }
                        if (amtEop.compareTo(temp) == 1) {
                            // 贷方累加和 抵小
                            iterator1.remove();
                            Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> map = new HashMap<>();
                            detailObj.setDiffEop(amtEop.subtract(temp));
                            map.put(detailObj, tradeObjList);
                            diffList.add(map);
                            continue;
                        }
                        if (amtEop.compareTo(temp) == -1) {
                            // 贷方需要排列组合
                            Map<BigDecimal, List<String>> treeMap = comboList(tradeObjList);
                            if (treeMap.get(amtEop) != null) {
                                List<String> idString = treeMap.get(amtEop);
                                String[] idArr = idString.get(0).split("-");
                                Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> map = new HashMap<>();
                                List<KpmgChkactSrcdtl> trades = new ArrayList<>();
                                for(KpmgChkactSrcdtl item : tradeObjList) {
                                    for (int k = 0; k < idArr.length; k++) {
                                        if (item.getDtlId().equals(idArr[k])) {
                                            trades.add(item);
                                        }
                                        Iterator<String> valueIterator = idString.iterator();
                                        while (valueIterator.hasNext()) {
                                            String tempValue = valueIterator.next();
                                            if (tempValue.contains(idArr[k])) {
                                                valueIterator.remove();
                                            }
                                        }
                                    }
                                }
                                detailObj.setDiffEop(BigDecimal.ZERO);
                                map.put(detailObj, trades);
                                diffList.add(map);
                                continue;
                            } else {
                                // 取组合最小值
                                boolean flag = true;
                                Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> map = new HashMap<>();
                                for (Map.Entry<BigDecimal, List<String>> item : treeMap.entrySet()) {
                                    if (amtEop.compareTo(item.getKey()) == 1) {
                                        detailObj.setDiffEop(amtEop.subtract(item.getKey()));
                                        List<String> idString = item.getValue();
                                        String[] idArr = idString.get(0).split("-");
                                        List<KpmgChkactSrcdtl> trades = new ArrayList<>();
                                        for(KpmgChkactSrcdtl item2 : tradeObjList) {
                                            for (int k = 0; k < idArr.length; k++) {
                                                if (item2.getDtlId().equals(idArr[k])) {
                                                    trades.add(item2);
                                                }
                                                Iterator<String> valueIterator = idString.iterator();
                                                while (valueIterator.hasNext()) {
                                                    String tempValue = valueIterator.next();
                                                    if (tempValue.contains(idArr[k])) {
                                                        valueIterator.remove();
                                                    }
                                                }
                                            }
                                        }
                                        map.put(detailObj, trades);
                                        diffList.add(map);
                                        flag = false;
                                        break;
                                    }
                                }

                                if (flag) {
                                    // 资产比负债小 资产做单边记账
                                    List<String> idString = treeMap.get((BigDecimal) treeMap.keySet().toArray()[treeMap.size() - 1]);
                                    String[] idArr = idString.get(0).split("-");
                                    List<KpmgChkactSrcdtl> trades = new ArrayList<>();
                                    for(KpmgChkactSrcdtl item2 : tradeObjList) {
                                        for (int k = 0; k < idArr.length; k++) {
                                            if (item2.getDtlId().equals(idArr[k])) {
                                                trades.add(item2);
                                            }
                                            Iterator<String> valueIterator = idString.iterator();
                                            while (valueIterator.hasNext()) {
                                                String tempValue = valueIterator.next();
                                                if (tempValue.contains(idArr[k])) {
                                                    valueIterator.remove();
                                                }
                                            }
                                        }
                                    }
                                    map.put(detailObj, trades);
                                    diffList.add(map);
                                }
                            }
                        }
                    }
                } else {
                    System.out.println(detailObj.getCorpNm() + "id" + detailObj.getDtlId() + "单边记账");
                    oneSideList.add(detailObj);
                    iterator1.remove();
                }
            }
        }

//        System.out.println(oneSideList);
//        System.out.println(diffList);
        Statement pstmt = null;
        String sqlStr = "";

//        for (Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> item : diffList) {
//            for (Map.Entry<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> entry : item.entrySet()) {
//                KpmgChkactSrcdtl key = entry.getKey();
//                System.out.println(key);
//                List<KpmgChkactSrcdtl> value = entry.getValue();
//                for (KpmgChkactSrcdtl valueItem : value) {
//                    System.out.println(valueItem);
//                }
//            }
//        }
//
        try {
            connection.setAutoCommit(false);
            pstmt = connection.createStatement();
//            for (KpmgChkactSrcdtl item : oneSideList) {
//                String uuid = this.getUUID();
//                // 插入匹配批次表
//                sqlStr = StatementUtils.getStatement("INSERT_BTCH");
//                sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
//                sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-单边记账");
//                sqlStr = sqlStr.replace("#{amtEop}", item.getAmtEop().toString());
//                sqlStr = sqlStr.replace("#{amtOfst}", item.getAmtEop().toString());
//                pstmt.addBatch(sqlStr);
//                // 插入本方记录表
//                sqlStr = StatementUtils.getStatement("INSERT_SELF");
//                sqlStr = sqlStr.replace("#{chkId}", uuid);
//                sqlStr = sqlStr.replace("#{acgYr}", item.getAcgYr());
//                sqlStr = sqlStr.replace("#{acgMo}", item.getAcgMo());
//                sqlStr = sqlStr.replace("#{corpCd}", item.getCorpCd());
//                sqlStr = sqlStr.replace("#{corpNm}", item.getCorpNm());
//                sqlStr = sqlStr.replace("#{cstCd}", item.getCstCd());
//                sqlStr = sqlStr.replace("#{cstNm}", item.getCstNm());
//                sqlStr = sqlStr.replace("#{sbjCd}", item.getSbjCd());
//                sqlStr = sqlStr.replace("#{sbjNm}", item.getSbjNm());
//                sqlStr = sqlStr.replace("#{sbjLvl}", item.getSbjLvl());
//                sqlStr = sqlStr.replace("#{sbjTpCd}", item.getSbjTpCd());
//                sqlStr = sqlStr.replace("#{sbjTpNm}", item.getSbjTpNm());
//                sqlStr = sqlStr.replace("#{amtEop}", item.getAmtEop().toString());
//                sqlStr = sqlStr.replace("#{amtOfst}", "0");
//                sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-单边记账");
//                sqlStr = sqlStr.replace("#{mtchRmrk}", "");
//                sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
//                pstmt.addBatch(sqlStr);
//                // 更新明细表中的本方记录
//                sqlStr = StatementUtils.getStatement("UPDATE_DETAIL");
//                sqlStr = sqlStr.replace("#{dtlId}", item.getDtlId());
//                pstmt.addBatch(sqlStr);
//            }

            for (Map<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> item : diffList) {
                for (Map.Entry<KpmgChkactSrcdtl, List<KpmgChkactSrcdtl>> entry : item.entrySet()) {
                    KpmgChkactSrcdtl key = entry.getKey();
                    String uuid = this.getUUID();
                    // 插入匹配批次表
                    sqlStr = StatementUtils.getStatement("INSERT_BTCH");
                    sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
                    sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-抵小抵消");
                    sqlStr = sqlStr.replace("#{amtEop}", key.getAmtEop().toString());
                    sqlStr = sqlStr.replace("#{amtOfst}", key.getAmtEop().toString());
                    pstmt.addBatch(sqlStr);
                    // 插入本方记录表
                    sqlStr = StatementUtils.getStatement("INSERT_SELF");
                    sqlStr = sqlStr.replace("#{chkId}", uuid);
                    sqlStr = sqlStr.replace("#{acgYr}", key.getAcgYr());
                    sqlStr = sqlStr.replace("#{acgMo}", key.getAcgMo());
                    sqlStr = sqlStr.replace("#{corpCd}", key.getCorpCd());
                    sqlStr = sqlStr.replace("#{corpNm}", key.getCorpNm());
                    sqlStr = sqlStr.replace("#{cstCd}", key.getCstCd());
                    sqlStr = sqlStr.replace("#{cstNm}", key.getCstNm());
                    sqlStr = sqlStr.replace("#{sbjCd}", key.getSbjCd());
                    sqlStr = sqlStr.replace("#{sbjNm}", key.getSbjNm());
                    sqlStr = sqlStr.replace("#{sbjLvl}", key.getSbjLvl());
                    sqlStr = sqlStr.replace("#{sbjTpCd}", key.getSbjTpCd());
                    sqlStr = sqlStr.replace("#{sbjTpNm}", key.getSbjTpNm());
                    sqlStr = sqlStr.replace("#{amtEop}", key.getAmtEop().toString());
                    sqlStr = sqlStr.replace("#{amtOfst}", key.getDiffEop().toString());
                    sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-抵小抵消");
                    sqlStr = sqlStr.replace("#{mtchRmrk}", "");
                    sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
                    pstmt.addBatch(sqlStr);
                    // 更新明细表中的本方记录
                    sqlStr = StatementUtils.getStatement("UPDATE_DETAIL");
                    sqlStr = sqlStr.replace("#{dtlId}", key.getDtlId());
                    pstmt.addBatch(sqlStr);

                    List<KpmgChkactSrcdtl> value = entry.getValue();
                    for (KpmgChkactSrcdtl valueItem : value) {
                        // 插入交易对手表
                        String uuid2 = this.getUUID();
                        sqlStr = StatementUtils.getStatement("INSERT_TRADE");
                        sqlStr = sqlStr.replace("#{chkId}", uuid2);
                        sqlStr = sqlStr.replace("#{acgYr}", valueItem.getAcgYr());
                        sqlStr = sqlStr.replace("#{acgMo}", valueItem.getAcgMo());
                        sqlStr = sqlStr.replace("#{corpCd}", valueItem.getCorpCd());
                        sqlStr = sqlStr.replace("#{corpNm}", valueItem.getCorpNm());
                        sqlStr = sqlStr.replace("#{cstCd}", valueItem.getCstCd());
                        sqlStr = sqlStr.replace("#{cstNm}", valueItem.getSbjNm());
                        sqlStr = sqlStr.replace("#{sbjCd}", valueItem.getSbjCd());
                        sqlStr = sqlStr.replace("#{sbjNm}", valueItem.getSbjNm());
                        sqlStr = sqlStr.replace("#{sbjLvl}", valueItem.getSbjLvl());
                        sqlStr = sqlStr.replace("#{sbjTpCd}", valueItem.getSbjTpCd());
                        sqlStr = sqlStr.replace("#{sbjTpNm}", valueItem.getSbjTpNm());
                        sqlStr = sqlStr.replace("#{amtEop}", valueItem.getAmtEop().toString());
                        sqlStr = sqlStr.replace("#{amtOfst}", "0");
                        sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-抵小抵消");
                        sqlStr = sqlStr.replace("#{mtchRmrk}", "");
                        sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
                        pstmt.addBatch(sqlStr);
                        // 更新明细表中的交易对手记录
                        sqlStr = StatementUtils.getStatement("UPDATE_DETAIL");
                        sqlStr = sqlStr.replace("#{dtlId}", valueItem.getDtlId());
                        pstmt.addBatch(sqlStr);
                    }
                }
            }

            pstmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            logger.error("一对多抵消异常" + e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    // 主键
    private static final String SbjCrpndId = "SBJ_CRPND_ID";
    // 科目类型名称
    private static final String SbjTpNm = "SBJ_TP_NM";
    // 对应科目类型名称
    private static final String SbjTpNmCrpnd = "SBJ_TP_NM_CRPND";

    private String baseSql = "#{baseSql}";
    private String startSql = "#{start}";
    private String endSql = "#{end}";

    /**
     * 获取科目类型列表
     *
     * @return
     */
    @Override
    public ServerResponse getCourseList(int page, int size, SysUsr user, String param) {

        Map<String, Object> resultMap = new HashMap<>();
        if (user != null) {
            Project project = Utils.getProject(projectId, user);
            Connection connection = DatabaseUtils.getConnection(project);
            StringBuilder querySql = new StringBuilder("SELECT * FROM KPMG_SBJ_TP_CRPND");
            // 查询支持
            if (!StringUtils.isEmpty(param)) {
                querySql.append(" WHERE (SBJ_CRPND_ID LIKE '%").append(param).append("%'").append(" OR SBJ_TP_NM LIKE '%").append(param).append("%') ");
            }
            List<Map<String, Object>> resultList = null;
            if (page != 0 && size != 0) {
                String pagingSql = Constants.PAGING_SQL;
                int start = (page - 1) * size + 1;
                int end = page * size;
                pagingSql = pagingSql.replace(baseSql, querySql.toString())
                        .replace(startSql, String.valueOf(start)).replace(endSql, String.valueOf(end));
                resultList = DatabaseTemplate.queryForList(pagingSql, connection);
            } else {
                resultList = DatabaseTemplate.queryForList(querySql.toString(), connection);
            }

            List<KpmgSbjTpCrpnd> courseList = new ArrayList<>();
            for (Map<String, Object> item : resultList) {
                KpmgSbjTpCrpnd course = new KpmgSbjTpCrpnd();
                course.setSbjCrpndId(item.get(SbjCrpndId).toString());
                course.setSbjTpNm(item.get(SbjTpNm).toString());
                course.setSbjTpNmCrpnd(item.get(SbjTpNmCrpnd).toString());
                courseList.add(course);
            }


            if (courseList.size() > 0) {
                resultMap.put("more", true);
                resultMap.put("courseList", courseList);
                return ServerResponse.createBySuccess("获取科目列表成功", resultMap);
            }

        }
        // List<KpmgSbjTpCrpnd> courseList = courseMapper.selectCourseList();
        resultMap.put("more", false);
        return ServerResponse.createBySuccess("获取科目列表失败", resultMap);

    }

    /**
     * 一对一相等抵消
     * @param user
     */
    private void oneToOneCheck(SysUsr user, Connection connection) {
        List<Map<String, Object>> resultList = DatabaseTemplate.queryForList(StatementUtils.getStatement("QUERY_ONE_TO_ONE_CHECK"), connection);

        Statement pstmt = null;
        String sqlStr = "";

        try {
            connection.setAutoCommit(false);
            pstmt = connection.createStatement();

            for (Map<String, Object> item : resultList) {
                String uuid = this.getUUID();
                // 插入匹配批次表
                sqlStr = StatementUtils.getStatement("INSERT_BTCH");
                sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
                sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-相等抵消");
                sqlStr = sqlStr.replace("#{amtEop}", new BigDecimal(item.get("A_MONEY").toString()).add(new BigDecimal(item.get("B_MONEY").toString())).toString());
                sqlStr = sqlStr.replace("#{amtOfst}", new BigDecimal(item.get("A_MONEY").toString()).add(new BigDecimal(item.get("B_MONEY").toString())).toString());
                pstmt.addBatch(sqlStr);
                // 插入本方记录表
                sqlStr = StatementUtils.getStatement("INSERT_SELF");
                sqlStr = sqlStr.replace("#{chkId}", uuid);
                sqlStr = sqlStr.replace("#{acgYr}", item.get("A_YEAR").toString());
                sqlStr = sqlStr.replace("#{acgMo}", item.get("A_MONTH").toString());
                sqlStr = sqlStr.replace("#{corpCd}", item.get("A_CORPCD").toString());
                sqlStr = sqlStr.replace("#{corpNm}", item.get("A_CORPNAME").toString());
                sqlStr = sqlStr.replace("#{cstCd}", item.get("A_CSTCD").toString());
                sqlStr = sqlStr.replace("#{cstNm}", item.get("A_CSTNAME").toString());
                sqlStr = sqlStr.replace("#{sbjCd}", item.get("A_SBJCD").toString());
                sqlStr = sqlStr.replace("#{sbjNm}", item.get("A_SBJNAME").toString());
                sqlStr = sqlStr.replace("#{sbjLvl}", item.get("A_SBJLVL").toString());
                sqlStr = sqlStr.replace("#{sbjTpCd}", item.get("A_SBJTPCD").toString());
                sqlStr = sqlStr.replace("#{sbjTpNm}", item.get("A_SBJTPNAME").toString());
                sqlStr = sqlStr.replace("#{amtEop}", item.get("A_MONEY").toString());
                sqlStr = sqlStr.replace("#{amtOfst}", item.get("A_MONEY").toString());
                sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-相等抵消");
                sqlStr = sqlStr.replace("#{mtchRmrk}", "");
                sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
                pstmt.addBatch(sqlStr);
                // 更新明细表中的本方记录
                sqlStr = StatementUtils.getStatement("UPDATE_DETAIL");
                sqlStr = sqlStr.replace("#{dtlId}", item.get("A_ID").toString());
                pstmt.addBatch(sqlStr);
                // 插入交易对手表
                sqlStr = StatementUtils.getStatement("INSERT_TRADE");
                sqlStr = sqlStr.replace("#{chkId}", uuid);
                sqlStr = sqlStr.replace("#{acgYr}", item.get("B_YEAR").toString());
                sqlStr = sqlStr.replace("#{acgMo}", item.get("B_MONTH").toString());
                sqlStr = sqlStr.replace("#{corpCd}", item.get("B_CORPCD").toString());
                sqlStr = sqlStr.replace("#{corpNm}", item.get("B_CORPNAME").toString());
                sqlStr = sqlStr.replace("#{cstCd}", item.get("B_CSTCD").toString());
                sqlStr = sqlStr.replace("#{cstNm}", item.get("B_CSTNAME").toString());
                sqlStr = sqlStr.replace("#{sbjCd}", item.get("B_SBJCD").toString());
                sqlStr = sqlStr.replace("#{sbjNm}", item.get("B_SBJNAME").toString());
                sqlStr = sqlStr.replace("#{sbjLvl}", item.get("B_SBJLVL").toString());
                sqlStr = sqlStr.replace("#{sbjTpCd}", item.get("B_SBJTPCD").toString());
                sqlStr = sqlStr.replace("#{sbjTpNm}", item.get("B_SBJTPNAME").toString());
                sqlStr = sqlStr.replace("#{amtEop}", item.get("B_MONEY").toString());
                sqlStr = sqlStr.replace("#{amtOfst}", item.get("B_MONEY").toString());
                sqlStr = sqlStr.replace("#{mtchTpNm}", "自动-相等抵消");
                sqlStr = sqlStr.replace("#{mtchRmrk}", "");
                sqlStr = sqlStr.replace("#{mtchBtchId}", uuid);
                pstmt.addBatch(sqlStr);
                // 更新明细表中的交易对手记录
                sqlStr = StatementUtils.getStatement("UPDATE_DETAIL");
                sqlStr = sqlStr.replace("#{dtlId}", item.get("B_ID").toString());
                pstmt.addBatch(sqlStr);
            }
            pstmt.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            logger.error("一对一相等匹配异常" + e.getMessage(), e);
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    /**
     * 获取比较对象集合
     */
    private Map<String, List<KpmgChkactSrcdtl>> getObjList(SysUsr user, String year, String month, String type) {
        Project project = Utils.getProject(projectId, user);
        Connection connection = DatabaseUtils.getConnection(project);
        String querySql = "SELECT DTL_ID, ACG_YR, ACG_MO, CORP_CD, CORP_NM, CST_CD, CST_NM, SBJ_CD, SBJ_NM, SBJ_LVL," +
                "SBJ_TP_CD, SBJ_TP_NM, AMT_EOP, MTCH_IND FROM KPMG_CHKACT_SRCDTL WHERE ACG_YR = '#{acgYr}' AND ACG_MO = '#{acgMo}' AND SBJ_TP_CD = '#{sbjTpCd}' AND MTCH_IND = 0 ORDER BY CORP_NM";
        querySql = querySql.replace("#{acgYr}", year);
        querySql = querySql.replace("#{acgMo}", month);
        querySql = querySql.replace("#{sbjTpCd}", type);
        List<Map<String, Object>> resultList = DatabaseTemplate.queryForList(querySql, connection);
        Map<String, List<KpmgChkactSrcdtl>> resultMap = new HashMap<>();
        for (Map<String, Object> item : resultList) {
            KpmgChkactSrcdtl detail = new KpmgChkactSrcdtl();
            detail.setDtlId(item.get("DTL_ID").toString());
            detail.setAcgYr(item.get("ACG_YR").toString());
            detail.setAcgMo(item.get("ACG_MO").toString());
            detail.setCorpCd(item.get("CORP_CD").toString());
            detail.setCorpNm(item.get("CORP_NM").toString());
            detail.setCstCd(item.get("CST_CD").toString());
            detail.setCstNm(item.get("CST_NM").toString());
            detail.setSbjCd(item.get("SBJ_CD").toString());
            detail.setSbjNm(item.get("SBJ_NM").toString());
            detail.setSbjLvl(item.get("SBJ_LVL").toString());
            detail.setSbjTpCd(item.get("SBJ_TP_CD").toString());
            detail.setSbjTpNm(item.get("SBJ_TP_NM").toString());
            detail.setAmtEop(new BigDecimal(item.get("AMT_EOP").toString()));
            detail.setMtchInd(item.get("MTCH_IND").toString());
            if (!resultMap.containsKey(item.get("CORP_NM").toString())) {
                resultMap.put(item.get("CORP_NM").toString(), new ArrayList<KpmgChkactSrcdtl>());
            }
            resultMap.get(item.get("CORP_NM").toString()).add(detail);
        }

        return resultMap;

//        // 遍历每个月份
//        for (int i = 1; i <= 12; i++) {
//            // 设置本方名称
//            params.setCorpNm(compareName);
//            month = "0";
//            if (i < 10) {
//                month = month + i;
//            } else {
//                month = i + "";
//            }
//
//        }

        //System.out.println("====================>>>>>>对账成功>>>>>==================");
    }

    // 一对一相等匹配
    public static Map<String, List<KpmgChkactSrcdtl>> cal1(List<KpmgChkactSrcdtl> ourList, List<KpmgChkactSrcdtl> tradeList) {
        Iterator<KpmgChkactSrcdtl> ourIterator = ourList.iterator();
        while (ourIterator.hasNext()) {
            KpmgChkactSrcdtl ourObj = ourIterator.next();
            Iterator<KpmgChkactSrcdtl> tradeIterator = tradeList.iterator();
            while (tradeIterator.hasNext()) {
                KpmgChkactSrcdtl tradeObj = tradeIterator.next();
                if (ourObj.getAmtEop().compareTo(tradeObj.getAmtEop()) == 0) {
                    ourIterator.remove();
                    tradeIterator.remove();
                    System.out.println("一对一 本方id: " + ourObj.getDtlId() + "对方id:" + tradeObj.getDtlId());
                    break;
                }
            }
        }

        Map<String, List<KpmgChkactSrcdtl>> map = new HashMap<>();
        map.put("ourList", ourList);
        map.put("tradeList", tradeList);

        return map;
    }

    // 一对多组合相等匹配
    public static Map<String, List<KpmgChkactSrcdtl>> cal2(List<KpmgChkactSrcdtl> ourList, List<KpmgChkactSrcdtl> tradeList) {
        Map<BigDecimal, List<String>> treeMap = comboList(tradeList);

        Iterator<KpmgChkactSrcdtl> ourIterator = ourList.iterator();
        while (ourIterator.hasNext()) {
            KpmgChkactSrcdtl ourObj = ourIterator.next();
            Iterator<BigDecimal> mapIterator1 = treeMap.keySet().iterator();
            while (mapIterator1.hasNext()) {
                {
                    BigDecimal temp = mapIterator1.next();

                    if (ourObj.getAmtEop().compareTo(temp) == 0) {
                        System.out.println("本方id: " + ourObj.getDtlId() + "对方:id " + treeMap.get(temp).get(0));
                        // 移除本方相关id
                        ourIterator.remove();

                        String[] tradeIdArr = treeMap.get(temp).get(0).split("-");

                        // 移除对方
                        for (int i = 0; i < tradeIdArr.length; i++) {
                            // 移除treemap中匹配的
                            List<String> valueList = treeMap.get(temp);

                            Iterator<String> valueIterator = valueList.iterator();
                            while (valueIterator.hasNext()) {
                                String tempValue = valueIterator.next();
                                if (tempValue.contains(tradeIdArr[i])) {
                                    valueIterator.remove();
                                }
                            }
                            Map<BigDecimal, List<String>> newMap = new TreeMap<>();
                            newMap.put(ourObj.getAmtEop(), valueList);
                            treeMap = newMap;
                            // 移除对方id
                            Iterator<KpmgChkactSrcdtl> tradeIterator = tradeList.iterator();
                            while (tradeIterator.hasNext()) {
                                KpmgChkactSrcdtl tradeObj = tradeIterator.next();
                                if (tradeObj.getDtlId().equals(tradeIdArr[i])) {
                                    tradeIterator.remove();
                                }
                            }
                        }
                    }
                }
            }
        }
        Map<String, List<KpmgChkactSrcdtl>> map = new HashMap<>();
        map.put("ourList", ourList);
        map.put("tradeList", tradeList);

        return map;
    }

    // 一对多抵消匹配
    public static void cal3(List<KpmgChkactSrcdtl> ourList, List<KpmgChkactSrcdtl> tradeList) {
        //List<String> orderList = groupHandler(tradeList);
        Map<BigDecimal, List<String>> treeMap = comboList(tradeList);

        Iterator<KpmgChkactSrcdtl> ourIterator = ourList.iterator();

        while (ourIterator.hasNext()) {

            KpmgChkactSrcdtl ourPerson = ourIterator.next();
            Map<String, Object> resultMap = calNearNum(treeMap, ourPerson.getAmtEop());

            BigDecimal money = (BigDecimal) resultMap.get("money");
            List<String> matchList = (List<String>) resultMap.get("matchId");
            if (matchList.size() == 0) {
                continue;
            }
            String matchId = matchList.get(0);
            String[] matchIdArr = matchId.split("-");
            System.out.println("借方: " + ourPerson.getDtlId() + " 金额 " + ourPerson.getAmtEop() + " 贷方 " + matchId + "  金额" + money + " 差额 " + ourPerson.getAmtEop().subtract(money).toString());
            ourIterator.remove();
            if (tradeList.size() == 0) {
                break;
            }
            Iterator<BigDecimal> mapIterator = treeMap.keySet().iterator();

            while (mapIterator.hasNext()) {
                BigDecimal temp = mapIterator.next();
                List<String> valueList = treeMap.get(temp);
                if (valueList == null) {
                    break;
                }
                for (int i = 0; i < matchIdArr.length; i++) {
                    Iterator<String> valueIterator = valueList.iterator();
                    while (valueIterator.hasNext()) {
                        String tempValue = valueIterator.next();
                        if (tempValue.contains(matchIdArr[i])) {
                            valueIterator.remove();
                        }
                    }
                    Map<BigDecimal, List<String>> newMap = new TreeMap<>();
                    newMap.put(ourPerson.getAmtEop(), valueList);
                    treeMap = newMap;

                    Iterator<KpmgChkactSrcdtl> iterator = tradeList.iterator();
                    while (iterator.hasNext()) {
                        if (iterator.next().getDtlId().equals(matchIdArr[i])) {
                            iterator.remove();
                        }
                    }
                }

            }

        }

        if (tradeList.size() != 0 && ourList.size() > 0) {
            cal3(ourList, tradeList);
        } else {
            for (KpmgChkactSrcdtl item : ourList) {
                System.out.println("借方单边记账id  " + item.getDtlId() + "money " + item.getAmtEop());
            }

            for (KpmgChkactSrcdtl item : tradeList) {
                System.out.println("贷方单边记账id  " + item.getDtlId() + "money " + item.getAmtEop());
            }
        }

    }

    /**
     * @param treeMap
     * @param nearNum 求最接近的值
     */
    public static Map<String, Object> calNearNum(Map<BigDecimal, List<String>> treeMap, BigDecimal nearNum) {

        // BigDecimal firstNum = (BigDecimal) (treeMap.keySet()).toArray()[0];
        BigDecimal firstNum = treeMap.keySet().iterator().next();

        // 差值实始化
        BigDecimal diffNum = firstNum.subtract(nearNum).abs();
        // 最终结果
        BigDecimal result = null;
        List<String> matchId = new ArrayList<>();
        for (Map.Entry<BigDecimal, List<String>> entry : treeMap.entrySet()) {
            BigDecimal diffNumTemp = entry.getKey().subtract(nearNum).abs();
            if (diffNumTemp.compareTo(diffNum) == -1) {
                diffNum = diffNumTemp;
                result = entry.getKey();
                matchId = entry.getValue();
            }
        }

        if (result == null) {
            result = firstNum;
            matchId = treeMap.get(result);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("money", result);
        map.put("matchId", matchId);
        return map;
    }

    public static Map<BigDecimal, List<String>> comboList(List<KpmgChkactSrcdtl> list) {
        Map<BigDecimal, List<String>> resultMap = new TreeMap<BigDecimal, List<String>>(new Comparator<BigDecimal>() {
            @Override
            public int compare(BigDecimal o1, BigDecimal o2) {
                return o2.compareTo(o1);
            }
        });
        long n = (long)Math.pow(2,list.size());
        List<BigDecimal> combine;
        String indexStrCombo = null;
        for (long l = 0L; l < n; l++) {
            combine = new ArrayList<>();
            indexStrCombo = "";
            for (int i = 0; i < list.size(); i++) {
                if ((l >>> i & 1) == 1) {
                    KpmgChkactSrcdtl detail = list.get(i);
                    combine.add(detail.getAmtEop());
                    indexStrCombo += detail.getDtlId() + "-";
                }
            }
            if(combine.isEmpty()) {
                continue;
            }
            BigDecimal sumResult = accumulate(combine);
            if(!resultMap.containsKey(sumResult)) {
                resultMap.put(sumResult, new ArrayList<String>());
            }
            resultMap.get(sumResult).add(indexStrCombo.substring(0, indexStrCombo.lastIndexOf("-")));

        }
        return resultMap;
    }

    public static BigDecimal accumulate(List<BigDecimal> resultList) {
        BigDecimal total = new BigDecimal(0);
        for(BigDecimal bd : resultList) {
            total = total.add(bd);
        }
        return total;
    }

}
