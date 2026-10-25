import React, { createContext, useContext, useState, useEffect } from 'react';

type Language = 'en' | 'ar';

interface LanguageContextType {
  lang: Language;
  setLang: (lang: Language) => void;
  t: (key: string) => string;
  isRtl: boolean;
}

const translations: Record<Language, Record<string, string>> = {
  en: {
    system_title: 'School Attendance & Anti-Fraud System',
    login: 'Sign In',
    logout: 'Sign Out',
    username: 'Username',
    password: 'Password',
    dashboard: 'Dashboard',
    sessions: 'Attendance Sessions',
    anti_fraud: 'Anti-Fraud Engine',
    students: 'Students',
    teachers: 'Teachers',
    classes: 'Classes & Sections',
    excel_import: 'Excel Import',
    reports: 'Reports & Analytics',
    devices: 'Authorized Devices',
    audit_logs: 'Audit Logs',
    role_admin: 'Administrator',
    role_teacher: 'Teacher',
    role_student: 'Student',
    present: 'Present',
    late: 'Late',
    absent: 'Absent',
    excused: 'Excused',
    pending_confirmation: 'Pending Review',
    status: 'Status',
    date: 'Date',
    class: 'Class',
    subject: 'Subject',
    time: 'Time',
    actions: 'Actions',
    confirm: 'Confirm',
    reject: 'Reject',
    export_excel: 'Export Excel',
    export_csv: 'Export CSV',
    open_session: 'Start Attendance',
    close_session: 'Close Session',
    scan_qr: 'Scan QR Code',
    enter_code: 'Enter 6-Digit Code',
    countdown: 'Rotating in',
    seconds: 'seconds',
    offline_mode: 'Offline Mode Active',
    sync_now: 'Synchronize Now',
    synced: 'All records synchronized',
  },
  ar: {
    system_title: 'نظام إدارة الحضور ومكافحة التلاعب المدرسي',
    login: 'تسجيل الدخول',
    logout: 'تسجيل الخروج',
    username: 'اسم المستخدم',
    password: 'كلمة المرور',
    dashboard: 'لوحة التحكم',
    sessions: 'جلسات الحضور',
    anti_fraud: 'محرك مكافحة التلاعب',
    students: 'الطلاب',
    teachers: 'المعلمون',
    classes: 'الصفوف والشعب',
    excel_import: 'استيراد إكسل',
    reports: 'التقارير والإحصائيات',
    devices: 'الأجهزة المصرحة',
    audit_logs: 'سجل التدقيق',
    role_admin: 'مدير النظام',
    role_teacher: 'معلم',
    role_student: 'طالب',
    present: 'حاضر',
    late: 'متأخر',
    absent: 'غائب',
    excused: 'معذور',
    pending_confirmation: 'بانتظار التأكيد',
    status: 'الحالة',
    date: 'التاريخ',
    class: 'الشعبة',
    subject: 'المادة',
    time: 'الوقت',
    actions: 'الإجراءات',
    confirm: 'تأكيد الحضور',
    reject: 'رفض',
    export_excel: 'تصدير إكسل',
    export_csv: 'تصدير CSV',
    open_session: 'بدء تسجيل الحضور',
    close_session: 'إغلاق الجلسة',
    scan_qr: 'مسح رمز الاستجابة السريعة QR',
    enter_code: 'إدخال الرمز السداسي',
    countdown: 'التحديث بعد',
    seconds: 'ثانية',
    offline_mode: 'وضع العمل دون اتصال مفعّل',
    sync_now: 'مزامنة السجلات الآن',
    synced: 'جميع السجلات متزامنة',
  },
};

const LanguageContext = createContext<LanguageContextType | undefined>(undefined);

export const LanguageProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [lang, setLang] = useState<Language>(() => {
    return (localStorage.getItem('lang') as Language) || 'en';
  });

  const isRtl = lang === 'ar';

  useEffect(() => {
    localStorage.setItem('lang', lang);
    document.documentElement.dir = isRtl ? 'rtl' : 'ltr';
    document.documentElement.lang = lang;
  }, [lang, isRtl]);

  const t = (key: string) => {
    return translations[lang][key] || key;
  };

  return (
    <LanguageContext.Provider value={{ lang, setLang, t, isRtl }}>
      {children}
    </LanguageContext.Provider>
  );
};

export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) throw new Error('useLanguage must be used within LanguageProvider');
  return context;
}
