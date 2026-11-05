import {library} from "@fortawesome/fontawesome-svg-core";
import {
    faArrowLeft, faArrowRight, faBars, faBottleWater, faBox, faBoxesStacked,
    faCartArrowDown, faCashRegister, faChartPie, faCheck, faCheckDouble, faChevronDown,
    faChevronUp, faCircleExclamation, faCoins, faDownload, faEquals, faEraser,
    faEye, faEyeSlash, faGear, faHome, faLayerGroup, faListCheck,
    faMoneyBill, faMoneyBillWave, faMoneyBills, faMoon, faPen, faRightToBracket,
    faSackDollar, faSquareMinus, faSquarePlus, faStar, faSun, faTrashCan,
    faTriangleExclamation, faUpload, faUser, faUserGear, faUserPlus, faUsers,
    faWarehouse, faXmark
} from "@fortawesome/free-solid-svg-icons";

/**
 * Registers the icons the interface uses, so only these end up in the bundle instead of the whole icon set. An icon
 * used by name somewhere must be added here, or it renders empty.
 */
export function registerIcons() {
    library.add(
        faArrowLeft, faArrowRight, faBars, faBottleWater, faBox, faBoxesStacked,
        faCartArrowDown, faCashRegister, faChartPie, faCheck, faCheckDouble, faChevronDown,
        faChevronUp, faCircleExclamation, faCoins, faDownload, faEquals, faEraser,
        faEye, faEyeSlash, faGear, faHome, faLayerGroup, faListCheck,
        faMoneyBill, faMoneyBillWave, faMoneyBills, faMoon, faPen, faRightToBracket,
        faSackDollar, faSquareMinus, faSquarePlus, faStar, faSun, faTrashCan,
        faTriangleExclamation, faUpload, faUser, faUserGear, faUserPlus, faUsers,
        faWarehouse, faXmark
    )
}
