import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

export function toLocalDateLabel(utcIsoDate: string): string {
  return dayjs.utc(utcIsoDate).local().format('YYYY-MM-DD HH:mm');
}
