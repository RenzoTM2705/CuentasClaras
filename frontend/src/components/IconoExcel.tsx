interface Props {
    className?: string;
}

export default function IconoExcel({ className = 'h-8 w-8' }: Props) {
    return (
        <svg
            className={className}
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            aria-hidden="true"
        >
            <path
                d="M4 4.5A2.5 2.5 0 0 1 6.5 2H14l6 6v11.5a2.5 2.5 0 0 1-2.5 2.5h-11A2.5 2.5 0 0 1 4 19.5v-15Z"
                fill="#1D6F42"
            />
            <path d="M14 2l6 6h-4.5A1.5 1.5 0 0 1 14 6.5V2Z" fill="#0F4C2B" />
            <path
                d="M8 11.5h1.6l1.2 2 1.2-2H14l-2.1 3.3L14 18h-1.7l-1.5-2.4L9.3 18H7.6l2.1-3.2L7.6 11.5H8Z"
                fill="white"
            />
        </svg>
    );
}